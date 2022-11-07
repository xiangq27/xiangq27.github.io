/**
 * <p>Title: CPSC 433/533 Programming Assignment</p>
 *
 * <p>Description: Fishnet socket implementation</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Yale University</p>
 *
 * @author Hao Wang
 * @version 1.0
 */

import java.util.LinkedList;
import java.lang.reflect.Method;

public class TCPSock {
    // TCP socket states
    enum State {
        // protocol states
        CLOSED,
        LISTEN,
        SYN_SENT,
        ESTABLISHED,
        // auxiliary states
        NEW, // newly created, to distinguish from closed but not yet released
        SHUTDOWN, // close requested, FIN not sent (due to unsent data in queue)
        ANY
    }

    private TCPManager tcpMan;
    private TCPSockID id;
    private State state;

    // connection management

    // maximum segment size
    private int snd_MSS;
    // initial sequence number for sending (for debugging)
    private int snd_initseq;
    // initial sequence number for receiving (for debugging)
    private int rcv_initseq;
    // connection request queue (for listening sockets only)
    private LinkedList<TCPSock> pendingConnections;
    private int backlog;

    // we only implement 1-way byte stream, so only one of the following two
    // sets of fields are in use

    // the snd_buf and rcv_buf are used as circular queues of data,
    // so their sizes are the intented buffer size plus 1

    // for sending

    private byte[] snd_buf;
    private int snd_base;
    private int snd_top;
    // position of next byte to be write to the queue
    private int snd_next;

    // for receiving

    private byte[] rcv_buf;
    private int rcv_base;
    private int rcv_top;
    // position of next byte to be read from the queue
    private int rcv_next;

    // for retransmission

    // timeout interval, in milliseconds
    private long RTO;
    // sequence number of the segment being timed
    private int rt_seq;
    // number of pending retransmission timers for snd_base
    // only the last pending timer will fire, others are deemed as cancelled
    private int rt_pending;
    // number of retransmission timers expired for snd_base
    private int rt_expired;
    // number of duplicate ACKs, for fast retransmission
    private int dup_ack;
    // number of fast retransmission activated, to limit
    // the number of fast retransmission per RTO
    private int fast_ret;
    // RTO estimation
    private long RTT_estimate;
    private long RTTDev_estimate;
    // sequence number of the segment whose RTT is being measured
    private int RTT_sample_seq;
    // sending time of the segment whose RTT is being measured
    private long RTT_sample_send_time;

    // flow control

    // receiver's advertised window in bytes
    private int snd_rcvWnd;

    // congestion control

    // congestion window in MSS
    private float snd_cwnd;
    // slow start threshold in MSS
    private float snd_ssthresh;

    // sender window size in bytes
    private int snd_wnd;

    public TCPSock() {
        tcpMan = null;
        id = new TCPSockID();
        state = State.NEW;

        snd_MSS = TCPManager.DEFAULT_MSS;
        snd_initseq = 0;
        rcv_initseq = 0;
        pendingConnections = null;
        backlog = 0;

        // sending

        snd_buf = null;
        snd_base = 0;
        snd_top = 0;
        snd_next = 0;

        // receiving

        rcv_buf = null;
        rcv_base = 0;
        rcv_top = 0;
        rcv_next = 0;

        // retransmission

        RTO = TCPManager.INITIAL_RTO;
        rt_seq = 0;
        rt_pending = 0;
        rt_expired = 0;
        dup_ack = 0;
        fast_ret = 0;
        RTT_estimate = RTO;
        RTTDev_estimate = 0;
        RTT_sample_seq = -1;
        RTT_sample_send_time = 0;

        // flow control
        snd_rcvWnd = 0;

        // congestion control
        snd_cwnd = TCPManager.INITIAL_SND_CWND;
        snd_ssthresh = TCPManager.INITIAL_SND_SSTHRESH;

        update_snd_wnd();
    }

    public TCPManager getManager() {
        return tcpMan;
    }

    public void setManager(TCPManager tcpMan) {
        this.tcpMan = tcpMan;
    }

    public TCPSockID getID() {
        return id;
    }

    public void setID(TCPSockID id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    /*
     * The following are the socket APIs of TCP transport service.
     * All APIs are NON-BLOCKING.
     */

    /**
     * Bind a socket to a local port
     *
     * @param localPort int local port number to bind the socket to
     * @return int 0 on success, -1 otherwise
     */
    public int bind(int localPort) {
        if (id.getLocalPort() != TCPSockID.ANY_PORT) {
            // re-binding is not allowed
            return -1;
        }

        if (localPort == TCPSockID.ANY_PORT) {
            /**
             * @todo pick arbitrary unused port
             */
            return -1;
        }

        int localAddr = id.getLocalAddr();
        // check if localPort already in use
        TCPSock sock = tcpMan.getLocalSock(localAddr, localPort);
        if (sock != null) {
            return -1;
        } else {
            id.setLocalPort(localPort);
            return 0;
        }
    }

    /**
     * Listen for connections on a socket
     * @param backlog int Maximum number of pending connections
     * @return int 0 on success, -1 otherwise
     */
    public int listen(int backlog) {
        if (state != State.NEW) {
            // wrong socket state
            /**
             * @todo throw an exception
             */
            return -1;
        }

        if (id.getLocalPort() == TCPSockID.ANY_PORT) {
            // the socket is not bound to any port
            return -1;
        }

        pendingConnections = new LinkedList<TCPSock>();
        this.backlog = backlog;
        state = State.LISTEN;

        return 0;
    }

    /**
     * Accept a connection on a socket
     *
     * @return TCPSock The first established connection on the request queue
     */
    public TCPSock accept() {
        if (state != State.LISTEN) {
            // wrong socket state
            /**
             * @todo throw an exception
             */
            return null;
        }

        if (pendingConnections.isEmpty()) {
            return null;
        }

        return pendingConnections.removeFirst();
    }

    public boolean isConnectionPending() {
        return (state == State.SYN_SENT);
    }

    public boolean isClosed() {
        return (state == State.CLOSED);
    }

    public boolean isConnected() {
        return (state == State.ESTABLISHED);
    }

    public boolean isClosurePending() {
        return (state == State.SHUTDOWN);
    }

    /**
     * Initiate connection to a remote socket
     *
     * @param destAddr int Destination node address
     * @param destPort int Destination port
     * @return int 0 on success, -1 otherwise
     */
    public int connect(int destAddr, int destPort) {
        if (state != State.NEW) {
            // wrong socket state
            /**
             * @todo throw an exception
             */
            return -1;
        }

        if (id.getLocalPort() == TCPSockID.ANY_PORT) {
            // the socket has not been bound to any port
            return -1;
        }


        // initialize for sending

        id.setRemoteAddr(destAddr);
        id.setRemotePort(destPort);
        // create the sending (circular) queue
        snd_buf = new byte[TCPManager.DEFAULT_SEND_BUF_SZ + 1];
        // pick the initial sequence number
        snd_initseq = snd_base = snd_top = snd_next = tcpMan.initSeq(this);
        RTT_sample_seq = snd_base - 1;

        // the SYN counts as 1 byte
        snd_top += 1;
        state = State.SYN_SENT;
        // initiate the connection by sending out a SYN
        sendSYN();

        // do no wait for the ACK, check later by calling isConnectionPending()
        return 0;
    }

    /**
     * Initiate closure of a connection (graceful shutdown)
     */
    public void close() {
        if (state == State.LISTEN) {
            // release all established connections in queue
            while (!pendingConnections.isEmpty()) {
                TCPSock conn = pendingConnections.removeFirst();
                tcpMan.release(conn);
            }
            state = State.CLOSED;
        } else if (state == State.ESTABLISHED) {
            if (snd_base == snd_next) {
                // there are no data unsent in the queue, send the FIN now
                tcpMan.send(id, Transport.FIN, 0, snd_base, snd_buf, 0);
                state = State.CLOSED;
            }
            else {
                // there are unsent data in the queue, mark as shutdown only,
                // the FIN will be sent after all the queued data are sent
                state = State.SHUTDOWN;
            }
        } else if (state == State.SHUTDOWN) {
            // closure in progress, do nothing
        } else {
            // ANY, CLOSED, SYN_SENT, NEW
            state = State.CLOSED;
        }
    }

    /**
     * Release a connection immediately (abortive shutdown)
     */
    public void release() {
        close();
        if (state == State.SHUTDOWN) {
            // discard unsent data in the queue, send the FIN immediately
            tcpMan.send(id, Transport.FIN, 0, snd_base, snd_buf, 0);
        }

        // release this socket from the socket space
        tcpMan.release(this);
    }

    /**
     * Write to the socket up to len bytes from the buffer buf starting at
     * position pos.
     *
     * @param buf byte[] the buffer to write from
     * @param pos int starting position in buffer
     * @param len int number of bytes to write
     * @return int on success, the number of bytes written, which may be smaller
     *             than len; on failure, -1
     */
    public int write(byte[] buf, int pos, int len) {
        if (state == State.CLOSED) return -1;

        if (state != State.ESTABLISHED) {
            // wrong socket state
            /**
             * @todo throw an exception
             */
            return -1;
        }

        /* state == State.ESTABLISHED */

        if (snd_buf == null) {
            // this socket is not for sending
            return -1;
        }

        // amount of space available in queue
        int avail = (snd_buf.length - 1) - (snd_next - snd_base);
        // amount to write
        int cnt = Math.min(avail, len);

        // if nothing to write, return immediately
        if (cnt == 0) return 0;

        // write data to the queue
        qwrite(snd_buf, snd_next, buf, pos, cnt);

        int seq = snd_next;
        snd_next += cnt;
        while (seq < snd_base + snd_wnd && seq < snd_next) {
            // send all unsent data in window
            seq += sendDATA(seq);
        }

        return cnt;
    }

    /**
     * Read from the socket up to len bytes into the buffer buf starting at
     * position pos.
     *
     * @param buf byte[] the buffer
     * @param pos int starting position in buffer
     * @param len int number of bytes to read
     * @return int on success, the number of bytes read, which may be smaller
     *             than len; on failure, -1
     */
    public int read(byte[] buf, int pos, int len) {
        if (state == State.CLOSED) return -1;

        if (state != State.ESTABLISHED && state != State.SHUTDOWN) {
            // wrong socket state
            /**
             * @todo throw an exception
             */
            return -1;
        }

        /* state == State.ESTABLISHED || state == State.SHUTDOWN*/

        if (rcv_buf == null) {
            // this socket is not for receiving
            return -1;
        }

        // amount of data available in queue
        int avail = rcv_base - rcv_next;
        // amount to read
        int cnt = Math.min(avail, len);

        // if nothing to read, return immediately
        if (cnt == 0) return 0;

        // read data from the queue
        qread(rcv_buf, rcv_next, buf, pos, cnt);

        // advance window
        rcv_next += cnt;
        rcv_top += cnt;

        if (state == State.SHUTDOWN && rcv_next == rcv_base) {
            // now that all queued data have been read, close the socket
            state = State.CLOSED;
        }

        return cnt;
    }

    /*
     * End of socket API
     */

    /**
     * Process a newly arrived TCP segment
     *
     * @param srcAddr int Source node address of the segment
     * @param destAddr int Destination node address of the segment
     * @param segment Transport The newly arrived TCP segment
     */
    public void OnReceive(int srcAddr, int destAddr, Transport segment) {
        switch (segment.getType()) {
            case Transport.SYN:
                receiveSYN(srcAddr, destAddr, segment);
                break;

            case Transport.FIN:
                receiveFIN(srcAddr, destAddr, segment);
                break;

            case Transport.ACK:
                receiveACK(srcAddr, destAddr, segment);
                break;

            case Transport.DATA:
                receiveDATA(srcAddr, destAddr, segment);
                break;
        }
    }

    /**
     * Process a newly arrived SYN segment
     *
     * @param srcAddr int Source node address of the segment
     * @param destAddr int Destination node address of the segment
     * @param syn Transport The SYN segment
     */
    private void receiveSYN(int srcAddr, int destAddr, Transport syn) {
        System.out.print("S");

        if (state == State.LISTEN) {
            // the SYN for a new connection
            TCPSock conn = null;
            TCPSockID sid = new TCPSockID();
            sid.setLocalAddr(this.id.getLocalAddr());
            sid.setLocalPort(this.id.getLocalPort());
            sid.setRemoteAddr(srcAddr);
            sid.setRemotePort(syn.getSrcPort());
            int ackNum = syn.getSeqNum() + 1;

            if (pendingConnections.size() >= backlog ||
                (conn = tcpMan.socket()) == null) {
                // connection refused because either pending connection queue
                // is full, or no more free socket available
                tcpMan.send(sid, Transport.FIN, 0, ackNum, null, 0);
                return;
            }

            // accept the connection

            // initialize for receiving
            conn.setID(sid);
            // create the receiving (circular) queue
            conn.rcv_buf = new byte[TCPManager.DEFAULT_RECV_BUF_SZ + 1];
            conn.rcv_base = conn.rcv_next = ackNum;
            conn.rcv_top = conn.rcv_base + TCPManager.DEFAULT_RECV_BUF_SZ;
            conn.rcv_initseq = ackNum - 1;

            // send an ACK
            int rcv_wnd = conn.rcv_top - conn.rcv_base;
            tcpMan.send(sid, Transport.ACK, rcv_wnd, conn.rcv_base, null, 0);

            // put this established connection on the queue
            conn.setState(State.ESTABLISHED);
            pendingConnections.addLast(conn);
        } else if (state == State.ESTABLISHED &&
                   syn.getSeqNum() == rcv_initseq) {
            // duplicate SYN (ACK lost) arrive out of order, send an ACK
            int rcv_wnd = rcv_top - rcv_base;
            tcpMan.send(id, Transport.ACK, rcv_wnd, rcv_base, null, 0);
        }
    }

    /**
     * Process a newly arrived FIN segment
     *
     * @param srcAddr int Source node address of the segment
     * @param destAddr int Destination node address of the segment
     * @param fin Transport The FIN segment
     */
    private void receiveFIN(int srcAddr, int destAddr, Transport fin) {
        System.out.print("F");

        if (state == State.ESTABLISHED) {
            if (rcv_next == rcv_base) {
                // there are no unread data in the queue, mark as closed
                state = State.CLOSED;
            } else {
                // there are unread data in the queue, mark as shutdown only
                state = State.SHUTDOWN;
            }
        }
    }

    /**
     * Process a newly arrived ACK segment
     *
     * @param srcAddr int Source node address of the segment
     * @param destAddr int Destination node address of the segment
     * @param ack Transport The ACK segment
     */
    private void receiveACK(int srcAddr, int destAddr, Transport ack) {
        int ackNum = ack.getSeqNum();

        if (state == State.SYN_SENT && ackNum == snd_base + 1) {
            // connection established
            System.out.print(":");
            state = State.ESTABLISHED;
            snd_base = ackNum;
            // skip the SYN byte
            snd_next = ackNum;
            snd_rcvWnd = ack.getWindow();
            update_snd_wnd();
            return;
        }

        if (state != State.ESTABLISHED && state != State.SHUTDOWN) {
            // ACKs in wrong states, discard
            System.out.print("?");
            return;
        }

        /* state == State.ESTABLISHED || state == State.SHUTDOWN */

        if (ackNum > snd_top) {
            // ACK outside window
            System.out.print("?");
            return;
        }

        snd_rcvWnd = ack.getWindow();
        update_snd_wnd();

        if (ackNum > snd_base) {
            // new ACK
            System.out.print(":");

            rt_pending = 0;
            rt_expired = 0;
            dup_ack = 0;
            fast_ret = 0;

            if (RTT_sample_seq >= snd_base && ackNum > RTT_sample_seq) {
                // RTO estimation
                long RTT_sample = tcpMan.now() - RTT_sample_send_time;
                RTT_estimate *= (1 - TCPManager.RTT_ESTIMATE_RATE);
                RTT_estimate += RTT_sample * TCPManager.RTT_ESTIMATE_RATE;
                RTTDev_estimate *= (1 - TCPManager.RTTDEV_ESTIMATE_RATE);
                RTTDev_estimate += Math.abs(RTT_estimate - RTT_sample) *
                    TCPManager.RTTDEV_ESTIMATE_RATE;
                RTO = RTT_estimate + 4 * RTTDev_estimate;
                RTO = Math.max(RTO, TCPManager.RTO_MIN);
                RTO = Math.min(RTO, TCPManager.RTO_MAX);
            }

            int next_to_send = Math.min(snd_base + snd_wnd, snd_top);

            // congestion control
            if (snd_cwnd < snd_ssthresh) {
                // slow start
                // NOTE: ackNum is accumulative, so need to figure out
                //       how many segments are ACKed
                int acked = (ackNum - snd_base + snd_MSS - 1) / snd_MSS;
                snd_cwnd += acked;
            } else {
                // congestion avoidance, increase (approx.) by 1 per RTT
                snd_cwnd += 1 / snd_cwnd;
            }
            update_snd_wnd();

            // advance window
            snd_base = ackNum;

            if (snd_base < snd_next) {
                // there are unsent data in the queue

                if (snd_base < snd_top) {
                    // IMPORTANT:
                    // the old timer has been cancelled,
                    // must start a new timer on snd_base
                    start_rt_timer(snd_base);
                }

                int seq = Math.max(next_to_send, snd_base);
                while (seq < snd_base + snd_wnd && seq < snd_next) {
                    // send all unsent data in window
                    seq += sendDATA(seq);
                }
            } else /* snd_base == snd_next */ {
                if (state == State.SHUTDOWN) {
                    // now that all queued data have been sent,
                    // send the FIN and close the socket
                    tcpMan.send(id, Transport.FIN, 0, snd_base, snd_buf, 0);
                    state = State.CLOSED;
                }
            }

            return;
        } else /* ack <= snd_base */ {
            // duplicate ACK
            System.out.print("?");

            dup_ack++;
            if (dup_ack == TCPManager.DUP_ACK_THRESHOLD) {
                // cancel pending RTT measurement
                RTT_sample_seq = snd_base - 1;

                // congestion control: multiplicative decrease
                snd_cwnd = snd_ssthresh = Math.max(snd_cwnd / 2, 1);
                update_snd_wnd();

                if (fast_ret < TCPManager.FAST_RET_PER_RTO) {
                    // fast retransmission
                    for (int seq = snd_base;
                         seq < snd_base + snd_wnd && seq < snd_next; ) {
                        seq += sendDATA(seq);
                    }
                    fast_ret++;
                }
                // Note: one duplicate ACK activation per RTO
                //dup_ack = 0;
            }

            return;
        }
    }

    /**
     * Process a newly arrived DATA segment
     *
     * @param srcAddr int Source node address of the segment
     * @param destAddr int Destination node address of the segment
     * @param data Transport The DATA segment
     */
    private void receiveDATA(int srcAddr, int destAddr, Transport data) {
        int seqNum = data.getSeqNum();

        if (seqNum == rcv_base) {
            // in sequence data, accept
            System.out.print(".");
            byte[] payload = data.getPayload();
            int len = payload.length;
            // amount of space available in queue
            int avail = rcv_top - rcv_base;
            // amount to write
            int cnt = Math.min(avail, len);
            qwrite(rcv_buf, rcv_base, payload, 0, cnt);
            // advance window
            rcv_base += cnt;
        } else {
            // out of sequence data
            System.out.print("&");
        }

        // send the ACK
        int rcv_wnd = rcv_top - rcv_base;
        tcpMan.send(id, Transport.ACK, rcv_wnd, rcv_base, null, 0);
    }

    /**
     * Read from a circular queue
     *
     * @param src byte[]
     * @param srcPos int
     * @param dest byte[]
     * @param destPos int
     * @param len int
     */
    public static void qread(byte[] src, int srcPos,
                             byte[] dest, int destPos, int len) {
        // starting position in queues
        srcPos %= src.length;

        // transfer data
        if (srcPos + len > src.length) {
            // need to wrap around
            int cnt = src.length - srcPos;
            System.arraycopy(src, srcPos, dest, destPos, cnt);
            destPos += cnt;
            System.arraycopy(src, 0, dest, destPos, len - cnt);
        } else {
            System.arraycopy(src, srcPos, dest, destPos, len);
        }
    }

    /**
     * Write into a circular queue
     *
     * @param dest byte[]
     * @param destPos int
     * @param src byte[]
     * @param srcPos int
     * @param len int
     */
    public static void qwrite(byte[] dest, int destPos,
                              byte[] src, int srcPos, int len) {
        // starting position in queues
        destPos %= dest.length;

        // transfer data
        if (destPos + len > dest.length) {
            // need to wrap around
            int cnt = dest.length - destPos;
            System.arraycopy(src, srcPos, dest, destPos, cnt);
            srcPos += cnt;
            System.arraycopy(src, srcPos, dest, 0, len - cnt);
        } else {
            System.arraycopy(src, srcPos, dest, destPos, len);
        }
    }

    /**
     * Send a SYN
     */
    public void sendSYN() {
        if (state != State.SYN_SENT) return;
        tcpMan.send(id, Transport.SYN, 0, snd_base, snd_buf, 0);
        // if no ACK after SYN_TIMEOUT, re-send the SYN
        try {
            Method method = Callback.getMethod("sendSYN", this, null);
            Callback cb = new Callback(method, this, null);
            tcpMan.addTimer(TCPManager.SYN_TIMEOUT, cb);
        } catch (Exception e) {
            // This should not happen
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void update_snd_wnd() {
        int rwnd = Math.max(snd_rcvWnd, 1);
        int cwnd = Math.round(snd_cwnd) * snd_MSS;
        snd_wnd = Math.min(rwnd, cwnd);
    }

    private int sendDATA(int seq) {
        int unsent = snd_next - seq;
        int inWnd = snd_base + snd_wnd - seq;
        int cnt = Math.min(unsent, inWnd);
        cnt = Math.min(cnt, snd_MSS);
        tcpMan.send(id, Transport.DATA, 0, seq, snd_buf, cnt);
        int top = seq + cnt;
        if (snd_top < top) {
            snd_top = top;
        }

        if (seq == snd_base) {
            // only one timer for snd_base
            start_rt_timer(seq);
        }

        if (RTT_sample_seq < snd_base) {
            // start a new RTT measurement
            RTT_sample_seq = seq;
            RTT_sample_send_time = tcpMan.now();
        }

        return cnt;
    }

    private void start_rt_timer(int seq) {
        try {
            String[] paramTypes = {"java.lang.Integer"};
            Object[] params = {Integer.valueOf(seq)};
            Method method = Callback.getMethod("retransmit", this, paramTypes);
            Callback cb = new Callback(method, this, params);
            tcpMan.addTimer(RTO, cb);
            rt_pending++;
        } catch (Exception e) {
            // This should not happen
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void retransmit(Integer seqNum) {
        int seq = seqNum.intValue();

        if (snd_base > seq) {
            // timer has been cancelled, no retransmission
            return;
        }

        if (rt_pending-- > 1) {
            // only the last timer for snd_base should fire
            return;
        }

        // timer expires

        rt_expired++;
        dup_ack = 0;
        fast_ret = 0;

        // cancel pending RTT measurement
        RTT_sample_seq = snd_base - 1;

        // congestion control
        snd_ssthresh = Math.max(snd_cwnd / 2, 1);
        snd_cwnd = 1;
        update_snd_wnd();

        if (rt_expired == TCPManager.EXP_BACKOFF_THRESHOLD) {
            RTO *= 2;
            RTO = Math.min(RTO, TCPManager.RTO_MAX);
            rt_expired = 0;
        }

        // retransmit a full window of unsent data
        System.out.print("!");
        for (/*seq = snd_base*/; seq < snd_base + snd_wnd && seq < snd_next; ) {
            seq += sendDATA(seq);
        }
    }
}
