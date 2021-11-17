/**
 * <p>Title: CPSC 433/533 Programming Assignment</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Yale University</p>
 *
 * @author Hao Wang
 * @version 1.0
 */

/**
 * <p> A simple TCP socket space for Fishnet </p>
 */
public class SimpleTCPSockSpace {
    int sz;
    TCPSock[] socks;

    public SimpleTCPSockSpace() {
        this(TCPManager.MAX_SOCK_NUM);
    }

    public SimpleTCPSockSpace(int sz) {
        this.sz = sz;
        this.socks = new TCPSock[sz];
        for (int i = 0; i < sz; i++) {
            socks[i] = null;
        }
    }

    public TCPSock newSocket() {
        for (int i = 0; i < sz; i++) {
            if (socks[i] == null) {
                socks[i] = new TCPSock();
                return socks[i];
            }
        }

        return null;
    }

    public void release(TCPSock sock) {
        for (int i = 0; i < sz; i++) {
            if (socks[i] == sock) {
                socks[i] = null;
                return;
            }
        }
    }

    public TCPSock getLocalSock(int localAddr, int localPort,
                                TCPSock.State state) {
        for (int i = 0; i < sz; i++) {
            TCPSock sock = socks[i];
            if (sock == null) continue;
            TCPSockID id = sock.getID();
            if (id.getLocalAddr() == localAddr &&
                id.getLocalPort() == localPort &&
                (state == TCPSock.State.ANY || state == sock.getState())) {
                return sock;
            }
        }

        return null;
    }

    public TCPSock getSock(int localAddr, int localPort,
                           int remoteAddr, int remotePort) {
        for (int i = 0; i < sz; i++) {
            TCPSock sock = socks[i];
            if (sock == null) continue;
            TCPSockID id = sock.getID();
            if (id.getLocalAddr() == localAddr &&
                id.getLocalPort() == localPort &&
                id.getRemoteAddr() == remoteAddr &&
                id.getRemotePort() == remotePort) {
                return sock;
            }
        }

        return null;
    }
}
