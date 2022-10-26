/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package handlers;

import java.nio.channels.SocketChannel;

/**
 * Callback interface for receiving events from a Connector. 
 * 
 * @author Nuno Santos
 */
public interface ConnectorListener {
  /**
   * Called when the connection is fully established. 
   * @param connector The source of this event. 
   * @param sc The newly connected socket.
   */
  public void connectionEstablished(Connector connector, SocketChannel sc);
  /**
   * Called when the connection fails to be established.
   * @param connector The source of this event.
   * @param cause The cause of the error.
   */
  public void connectionFailed(Connector connector, Exception cause);
}
