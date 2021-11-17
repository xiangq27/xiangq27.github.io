/**
 * <p>Title: CPSC 433/533 Programming Assignment</p>
 *
 * <p>Description: Fishnet TCP socket identifier</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: Yale University</p>
 *
 * @author Hao Wang
 * @version 1.0
 */
public class TCPSockID {
    public static final int ANY_PORT = Transport.MAX_PORT_NUM + 1;
    public static final int ANY_ADDRESS = Packet.MAX_ADDRESS + 1;

    private int localAddr;
    private int localPort;
    private int remoteAddr;
    private int remotePort;

    public TCPSockID() {
        this.localAddr = ANY_ADDRESS;
        this.localPort = ANY_PORT;
        this.remoteAddr = ANY_ADDRESS;
        this.remotePort = ANY_PORT;
    }

    public int getLocalAddr() {
        return localAddr;
    }

    public void setLocalAddr(int localAddr) {
        this.localAddr = localAddr;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public int getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(int remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
