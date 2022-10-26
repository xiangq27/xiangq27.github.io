/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package handlers;

import io.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Manages a non-blocking connection attempt to a remote host. 
 * 
 * @author Nuno Santos
 */
final public class Connector implements ConnectorSelectorHandler {
  // The socket being connected.
  private SocketChannel sc;  
  // The address of the remote endpoint.
  private final InetSocketAddress remoteAddress;
  // The selector used for receiving events.
  private final SelectorThread selectorThread;
  // The listener for the callback events.
  private final ConnectorListener listener;
  
  /**
   * Creates a new instance. The connection is not attempted here.
   * Use connect() to start the attempt.
   * 
   * @param remoteAddress The remote endpoint where to connect.
   * @param listener The object that will receive the callbacks from
   * this Connector.
   * @param selector The selector to be used.
   * @throws IOException
   */
  public Connector(SelectorThread selector, 
      InetSocketAddress remoteAddress, 
      ConnectorListener listener)
  {     
    this.selectorThread = selector;
    this.remoteAddress = remoteAddress;
    this.listener = listener;
  }
  
  /**
   * Starts a non-blocking connection attempt.
   *   
   * @throws IOException
   */
  public void connect() throws IOException {
    sc = SocketChannel.open();  
    // Very important. Set to non-blocking. Otherwise a call
    // to connect will block until the connection attempt fails 
    // or succeeds.
    sc.configureBlocking(false);
    sc.connect(remoteAddress);
    
    // Registers itself to receive the connect event.
    selectorThread.registerChannelLater(
        sc,
        SelectionKey.OP_CONNECT, 
        this,
        new CallbackErrorHandler() {
          public void handleError(Exception ex) {    
            listener.connectionFailed(Connector.this, ex);
          }
        });
  }
    
  /**
   * Called by the selector thread when the connection is 
   * ready to be completed.
   */
  public void handleConnect() {
    try {
      if (!sc.finishConnect()) {
        // Connection failed
        listener.connectionFailed(this, null);
        return;
      }
      // Connection succeeded
      listener.connectionEstablished(this, sc);
    } catch (IOException ex) {      
      // Could not connect.
      listener.connectionFailed(this, ex);
    }
  }
  
  public String toString() {   
    return "Remote endpoint: " + 
      remoteAddress.getAddress().getHostAddress() + ":" + remoteAddress.getPort();
  }
}