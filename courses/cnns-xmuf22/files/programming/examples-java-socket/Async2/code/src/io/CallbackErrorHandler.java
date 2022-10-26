/*
  (c) 2004, Nuno Santos, nfsantos@sapo.pt
  relased under terms of the GNU public license 
  http://www.gnu.org/licenses/licenses.html#TOCGPL
*/
package io;

/**
 * Defines the callback to be used to handle errors in 
 * asynchronous method invocations.
 * 
 * When an operation is executed asynchronously it is not
 * possible to use exceptions for error handling. Instead,
 * the caller must provide a callback to be used in case
 * of error. 
 * 
 * @author Nuno Santos 
 */
public interface CallbackErrorHandler {
  /**
   * Called when an exception is raised when executing an
   * asynchronous method. 
   * @param ex
   */
  public void handleError(Exception ex);
}
