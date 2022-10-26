/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package handlers;

import io.SelectorThread;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * A simple server for demonstrating the IO Multiplexing framework 
 * in action. After accepting a connection, it will read packets 
 * as defined by the SimpleProtocolDecoder class and echo them back. 
 * 
 * This server can accept and manage large numbers of incoming 
 * connections. For added fun remove the System.out statements and 
 * try it with several thousand (>10.000) clients. You might have to 
 * increase the maximum number of sockets allowed by the operating 
 * system.
 * 
 * @author Nuno Santos 
 */
public class Server implements AcceptorListener, PacketChannelListener {
  private final SelectorThread st;  
  
  /**
   * Starts the server. 
   * 
   * @param listenPort The port where to listen for incoming connections.
   * @throws Exception
   */
  public Server(int listenPort) throws Exception {
    st = new SelectorThread();    
    Acceptor acceptor = new Acceptor(listenPort, st, this);
    acceptor.openServerSocket();
    System.out.println("Listening on port: " + listenPort);
  }  
  
  public static void main(String[] args) throws Exception {    
    int listenPort = Integer.parseInt(args[0]);
    new Server(listenPort);
  }
  
  //////////////////////////////////////////
  // Implementation of the callbacks from the 
  // Acceptor and PacketChannel classes
  //////////////////////////////////////////
  /**
   * A new client connected. Creates a PacketChannel to handle it.
   */
  public void socketConnected(Acceptor acceptor, SocketChannel sc) {    
    System.out.println("["+ acceptor + "] Socket connected: " + 
        sc.socket().getInetAddress());
    try {
      // We should reduce the size of the TCP buffers or else we will
      // easily run out of memory when accepting several thousands of
      // connctions
      sc.socket().setReceiveBufferSize(2*1024);
      sc.socket().setSendBufferSize(2*1024);
      // The contructor enables reading automatically.
      PacketChannel pc = new PacketChannel(
          sc, 
          st, 
          new SimpleProtocolDecoder(), 
          this);
      pc.resumeReading();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void socketError(Acceptor acceptor, Exception ex) {
    System.out.println("["+ acceptor + "] Error: " + ex.getMessage());
  }

  public void packetArrived(PacketChannel pc, ByteBuffer pckt) {
//    System.out.println("[" + pc.toString() + "] Packet received. Size: " + pckt.remaining());
    pc.sendPacket(pckt);
  }

  public void socketException(PacketChannel pc, Exception ex) {
    System.out.println("[" + pc.toString() + "] Error: " + ex.getMessage());    
  }

  public void socketDisconnected(PacketChannel pc) {
    System.out.println("[" + pc.toString() + "] Disconnected.");
  }

  /**
   * The answer to a request was sent. Prepare to read the 
   * next request. 
   */
  public void packetSent(PacketChannel pc, ByteBuffer pckt) {
    try {
      pc.resumeReading();
    } catch (Exception e) {    
      e.printStackTrace();
    }
  }
}
