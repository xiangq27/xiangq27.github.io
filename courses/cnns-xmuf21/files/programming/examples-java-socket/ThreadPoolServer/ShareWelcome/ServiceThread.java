/*
 * 
 * XMU CNNS Class Demo
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class ServiceThread extends Thread {

    ServerSocket welcomeSocket;

    public ServiceThread(ServerSocket welcomeSocket) {
	    this.welcomeSocket = welcomeSocket;
    }
  
    public void run() {

	    System.out.println("Thread " + this + " started.");
	    while (true) {
	        // get a new request connection
	        Socket s = null;

	        synchronized (welcomeSocket) {         
		        try {
		            s = welcomeSocket.accept();
		            System.out.println("Thread " + this 
				                       + " process request " + s);
		        } catch (IOException e) {
		        }
	        } // end of extract a request

	       serveARequest( s );
			
	    } // end while
		
    } // end run

    private void serveARequest(Socket connSock) {
    	
	    try {
	        // create read stream to get input
	        BufferedReader inFromClient = 
		       new BufferedReader(new InputStreamReader(connSock.getInputStream()));
	        String clientSentence = inFromClient.readLine();

	        // process input
	        String capitalizedSentence = clientSentence.toUpperCase() + '\n';

	        // send reply
	        DataOutputStream outToClient = 
	            new DataOutputStream(connSock.getOutputStream());
	        outToClient.writeBytes(capitalizedSentence);

	        connSock.close();

	        System.out.println("Finish a request");

	    } catch (Exception e) {
	        System.out.println("Exception happened in Thread " + this);
	    } // end of catch

    } // end of serveARequest

} // end ServiceThread
