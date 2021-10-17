import java.io.*;
import java.util.*;
import java.net.*;

public class RequestHandler implements Runnable {

    Socket connectionSocket;

    public RequestHandler(Socket connectionSocket) 
    {
    	System.out.println("New " + this);

		this.connectionSocket = connectionSocket;
    }

    public void run() {

	try {
	    // create read stream to get input
	    BufferedReader inFromClient = 
	 		new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
	    
	    String clientSentence = inFromClient.readLine();
	    
	    // process input
	    String capitalizedSentence = clientSentence.toUpperCase() + '\n';

	    // send reply
	    DataOutputStream outToClient = 
			new DataOutputStream(connectionSocket.getOutputStream());
	    outToClient.writeBytes(capitalizedSentence);

	    connectionSocket.close();

	    System.out.println(this + " exits");
	}
	catch (Exception e) {}
    }
}