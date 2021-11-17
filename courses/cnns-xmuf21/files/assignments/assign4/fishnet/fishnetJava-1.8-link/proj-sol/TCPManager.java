/**
 * <p>Title: CPSC 433/533 Programming Assignment</p>
 *
 * <p>Description: Fishnet TCP manager</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Yale University</p>
 *
 * @author Hao Wang
 * @version 1.0
 */
public class TCPManager {
    public static final int MAX_SOCK_NUM = 8;
    public static final int DEFAULT_SEND_BUF_SZ = 16384;
    public static final int DEFAULT_RECV_BUF_SZ = 16384;
    public static final int DEFAULT_MSS = Transport.MAX_PAYLOAD_SIZE;
    public static final int INITIAL_RTO = 1000;
    public static final int SYN_TIMEOUT = 1000;
    public static final int INITIAL_SND_WND = DEFAULT_MSS * 100;
    public static final double RTT_ESTIMATE_RATE = 0.125;
    public static final double RTTDEV_ESTIMATE_RATE = 0.25;
    public static final long RTO_MIN = 50;
    public static final long RTO_MAX = 30000;
    public static final int DUP_ACK_THRESHOLD = 3;
    public static final int FAST_RET_PER_RTO = 1;
    public static final float INITIAL_SND_CWND = 1;
    public static final float INITIAL_SND_SSTHRESH = 16384;
    public static final int EXP_BACKOFF_THRESHOLD = 1;

    private Node node;
    private int addr;
    private Manager manager;

    private SimpleTCPSockSpace socks;

    private static final byte dummy[] = new byte[0];

    public TCPManager(Node node, int addr, Manager manager) {
        this.node = node;
        this.addr = addr;
        this.manager = manager;
        this.socks = new SimpleTCPSockSpace(MAX_SOCK_NUM);
    }

    /**
     * Start this TCP manager
     */
    public void start() {

    }

    /*
     * Begin socket API
     */

    /**
     * Create a socket
     *
     * @return TCPSock the newly created socket, which is not yet bound to
     *                 a local port
     */
    public TCPSock socket() {
        TCPSock sock = socks.newSocket();
        if (sock != null) {
            sock.setManager(this);
            TCPSockID sid = sock.getID();
            sid.setLocalAddr(this.addr);
        }

        return sock;
    }

    /*
     * End Socket API
     */

    /**
     * Release a socket
     *
     * @param sock TCPSock The socket to be released
     */
    public void release(TCPSock sock) {
        socks.release(sock);
    }

    /*
     * Socket space lookup routines
     */

    public TCPSock getLocalSock(int localAddr, int localPort,
                                TCPSock.State state) {
        return socks.getLocalSock(localAddr, localPort, state);
    }

    public TCPSock getLocalSock(int localAddr, int localPort) {
        return socks.getLocalSock(localAddr, localPort, TCPSock.State.ANY);
    }

    public TCPSock getSock(int localAddr, int localPort,
                           int remoteAddr, int remotePort) {
        return socks.getSock(localAddr, localPort, remoteAddr, remotePort);
    }

    /**
     * Demultiplex an incoming TCP segment
     *
     * @param srcAddr int Source node address of the segment
     * @param destAddr int Destination node address of the segment
     * @param segment Transport The incoming TCP segment
     */
    public void OnReceive(int srcAddr, int destAddr, Transport segment) {
        int srcPort = segment.getSrcPort();
        int destPort = segment.getDestPort();

        // TCP demultiplexing
        TCPSock sock = getSock(destAddr, destPort, srcAddr, srcPort);
        if (sock != null) {
            sock.OnReceive(srcAddr, destAddr, segment);
            return;
        }

        // A listening socket can match any remote address & port for SYN
        if (segment.getType() == Transport.SYN) {
            sock = getLocalSock(destAddr, destPort, TCPSock.State.LISTEN);
            if (sock != null) {
                sock.OnReceive(srcAddr, destAddr, segment);
                return;
            }
        }

        // siliently drop segments destined for nonexistent socket

        return;
    }

    /**
     * Send a transport segment to the specified node
     *
     * @param srcAddr int Source node address
     * @param destAddr int Destination node address
     * @param segment Transport Transport segment to be sent
     */
    public void send(TCPSockID sid, int type, int window, int seq,
                     byte[] snd_buf, int len) {
        byte[] payload;
        if (len > 0) {
            payload = new byte[len];
            TCPSock.qread(snd_buf, seq, payload, 0, len);
        } else {
            payload = this.dummy;
        }

        Transport segment = new
            Transport(sid.getLocalPort(), sid.getRemotePort(), type, window,
                      seq, payload);
        this.node.sendSegment(sid.getLocalAddr(), sid.getRemoteAddr(),
                              Protocol.TRANSPORT_PKT, segment.pack());
    }

    /**
     * Pick an initial sequence number for a socket
     *
     * @param sock TCPSock The socket requesting the initial sequence number
     * @return int
     */
    public int initSeq(TCPSock sock)  {
        /**
         * @todo sequence number management
         */
        return 0;
    }

    public void addTimer(long deltaT, Callback callback) {
        manager.addTimerAt(addr, manager.now() + deltaT, callback);
    }

    public long now() {
        return manager.now();
    }
}
