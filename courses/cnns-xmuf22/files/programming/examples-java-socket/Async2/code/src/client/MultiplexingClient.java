/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package client;

import handlers.*;
import io.SelectorThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * A simple test client for the I/O Multiplexing based server.
 * This class simulates several clients using an imaginary request-
 * reply protocol to connect to a server. Several connections can 
 * be established at the same time. Each one, generates a random packet 
 * to simulate a request, sends it to the server and waits for the 
 * answer before sending the next packet.
 * 
 * This implementation is based on I/O Multiplexing, so it should
 * be able to handle several thousand of connections. Using Redhat 
 * Linux 9.0 I was able to establish about 10.000 connections before 
 * running out of memory (the system had only 256Mb of RAM).  
 * 
 * @author Nuno Santos
 */
public class MultiplexingClient implements ConnectorListener, PacketChannelListener {
  /** A single selector for all clients */
  private static SelectorThread st;  
  /** Maximum size of the packets sent */
  private static final int MAX_SIZE = 10*1024;
  /** Minimum size of the packets sent */
  private static final int MIN_SIZE = 128;
  /** How many packets each client should send */ 
  private static final int PACKETS_TO_SEND = 20;
  /** For generating random packet sizes. */
  private static final Random r = new Random();
  /** How many connections to created */
  private static int connectionCount;
  /** How many connections were opened so far */
  private static int connectionsEstablished = 0;
  private static int connectionsFailed = 0;
  /** How many connections were disconnected so far */
  private static int connectionsClosed = 0;
  /** 
   * Keeps a list of connections that have been established but
   * not yet started.
   */
  private static List establishedConnections = new ArrayList(512); 

  /** How many packets each instance sent so far. */
  private int packetsSent = 0;
  
  /**
   * Initiates a non-blocking connection attempt to the given address.
   * 
   * @param remotePoint Where to try to connect.
   * @throws Exception
   */
  public MultiplexingClient(InetSocketAddress remotePoint) throws Exception {    
    Connector connector = new Connector(st, remotePoint, this);
    connector.connect();
    System.out.println("["+ connector + "] Connecting...");
  }
  
  /**
   * Creates a new packet with a size chosen randomly between
   * MIN_SIZE and MAX_SIZE. 
   */
  private ByteBuffer generateNextPacket() {
    // Generate a random size between 
    int size = MIN_SIZE + r.nextInt(MAX_SIZE-MIN_SIZE);
    ByteBuffer buffer = ByteBuffer.allocate(size);
    buffer.put(SimpleProtocolDecoder.STX);
    for (int i = 0; i < size-2; i++) {
      buffer.put((byte)'a');
    }
    buffer.put(SimpleProtocolDecoder.ETX);
    buffer.limit(buffer.position());
    buffer.flip();
    return buffer;
  }
  
  //////////////////////////////////////////
  // Implementation of the callbacks from the 
  // Acceptor and PacketChannel classes
  //////////////////////////////////////////
  /**
   * A new client connected. Creates a PacketChannel to handle it.
   */
  public void connectionEstablished(Connector connector, SocketChannel sc) {    
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
      
      // Do not start sending packets right away. Waits for all sockets
      // to connect. Otherwise, the load created by sending and receiving
      // packets will increase dramatically the time taken for all 
      // connections to be established. It is better to establish all 
      // connections and only then to start sending packets.
      establishedConnections.add(pc);      
      connectionsEstablished++;
      System.out.println("["+ connector + "] Connected: " + 
          sc.socket().getInetAddress() + 
          " (" + connectionsEstablished +"/"+connectionCount + ")");
      // If if all connections are established.
      checkAllConnected();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void connectionFailed(Connector connector, Exception cause) {
    System.out.println("["+ connector + "] Error: " + cause.getMessage());
    connectionsFailed++;
    checkAllConnected();
  }
  
  public void packetArrived(PacketChannel pc, ByteBuffer pckt) {
//    System.out.println("["+ pc.toString() + "] Packet arrived");
    if (packetsSent >= PACKETS_TO_SEND) {  
      // This connection sent all packets that it was supposed to send.
      // Close.
      System.out.println("[" + pc.getSocketChannel().socket().getLocalPort() + 
          "] Closed. Packets sent " + PACKETS_TO_SEND + 
          ". Connection: " + (connectionsClosed+1) + "/" + connectionsEstablished);
      pc.close();
      connectionClosed();
    } else {
      // Still more packets to send.
      sendPacket(pc);
    }
  }

  public void socketException(PacketChannel pc, Exception ex) {
    System.out.println("[" + pc.toString() + "] Error: " + ex.getMessage());
    connectionClosed();
  }

  public void socketDisconnected(PacketChannel pc) {
    System.out.println("[" + pc.toString() + "] Disconnected.");
    connectionClosed();
  }
  
  /**
   * The request was sent. Prepare to read the answer. 
   */
  public void packetSent(PacketChannel pc, ByteBuffer pckt) {
//    System.out.println("[" + pc.toString() + "] Packet sent.");
    try {
      pc.resumeReading();
    } catch (Exception e) {    
      e.printStackTrace();
    }
  }

  ////////////////////////////
  // Helper methods
  ////////////////////////////
  /**
   * Called when a connection is closed. Checks if all connections
   * have been closed and if so exits the virtual machine. 
   */
  private void connectionClosed() {
    connectionsClosed++;
    if (connectionsClosed >= connectionsEstablished)  {
      st.requestClose();      
      System.exit(1);
    }
  }
  
  /**
   * Sends a newly generated packet using the given PacketChannel
   *  
   * @param pc
   */
  private void sendPacket(PacketChannel pc) {
//    System.out.println("[" + pc.toString() + "] Sending packet.");
    ByteBuffer packet = generateNextPacket();
    packetsSent++;
    pc.sendPacket(packet);    
  }
  
  /**
   * Checks if all connections have been established. If so,
   * starts them all by sending an initial packet to all of them.
   */
  private void checkAllConnected() {   
    // Starts sending packets only after all connections are established.
    if ((connectionsEstablished+connectionsFailed) == connectionCount) {
      for (int i = 0; i < establishedConnections.size(); i++) {
        PacketChannel pc = (PacketChannel) establishedConnections.get(i);
        sendPacket(pc);
      }
      establishedConnections.clear();
    }
  }


  public static void main(String[] args) throws Exception {    
    InetSocketAddress remotePoint = 
      new InetSocketAddress(args[0], Integer.parseInt(args[1]));
    connectionCount = 1;
    if (args.length > 2) {
      connectionCount = Integer.parseInt(args[2]);
    }        
    st = new SelectorThread();
    for (int i = 0; i < connectionCount; i++) {      
      new MultiplexingClient(remotePoint);
      // Must sleep for a while between opening connections in order
      // to give the remote host enough time to handle them. Otherwise,
      // the remote host backlog will get full and the connection
      // attemps will start to be refused.
      Thread.sleep(10);
    }
  }
}