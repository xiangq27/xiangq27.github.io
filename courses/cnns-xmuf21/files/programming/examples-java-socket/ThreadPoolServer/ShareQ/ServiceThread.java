/*
 * 
 * CS433/533 Demo
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class ServiceThread extends Thread {

    private List<Socket> pool;

    public ServiceThread(List<Socket> pool) {
	    this.pool = pool;
    }
  
    public void run() {
    	
	    while (true) {
	        // get a new request connection
	        Socket s = null;

	        while (s == null) {
		        synchronized (pool) {         
		            if (!pool.isEmpty()) {
			           // remove the first request
			           s = (Socket) pool.remove(0); 
			           System.out.println("Thread " + this 
					       + " process request " + s);
		            } // end if
		        } // end of sync
	        } // end while
	        serveARequest( s );			
	    } // end while(true)

    } // end method run

    private void serveARequest(Socket connSock) {

	    try {
	        // create read stream to get input
	        BufferedReader inFromClient = 
		      new BufferedReader(new InputStreamReader(connSock.getInputStream()));
	        String clientSentence = inFromClient.readLine();

	        // process input
	        String capitalizedSentence = clientSentence.toUpperCase() + '\n';

	        // send reply
	        DataOutputStream outToClient = new DataOutputStream(connSock.getOutputStream());
	        outToClient.writeBytes(capitalizedSentence);
	    } catch (Exception e) {
	    	System.out.println("serveARequest failed.");
	    } // end of catch

    } // end of serveARequest

} // end ServiceThread
