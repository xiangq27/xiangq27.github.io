/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package handlers;

import java.nio.ByteBuffer;

/**
 * Callback interface for receiving events from a Connector. 
 * 
 * @author Nuno Santos
 */
public interface PacketChannelListener {
  /**
   * Called when a packet is fully reassembled.
   * 
   * @param pc The source of the event.
   * @param pckt The reassembled packet
   */
  public void packetArrived(PacketChannel pc, ByteBuffer pckt);
  
  /**
   * Called after finishing sending a packet.
   * 
   * @param pc The source of the event.
   * @param pckt The packet sent
   */
  public void packetSent(PacketChannel pc, ByteBuffer pckt);
  
  /**
   * Called when some error occurs while reading or writing to 
   * the socket.
   *  
   * @param pc The source of the event.
   * @param ex The exception representing the error.
   */
  public void socketException(PacketChannel pc, Exception ex);
    
  /**
   * Called when the read operation reaches the end of stream. This
   * means that the socket was closed.
   * 
   * @param pc The source of the event.
   */
  public void socketDisconnected(PacketChannel pc);
}
