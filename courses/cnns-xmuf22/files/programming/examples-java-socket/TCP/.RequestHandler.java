import java.io.*;
import java.net.*;

class RequestHandler implements Runnable {
    private Socket socket;

    public RequestHandler(Socket socket) {
	this.socket = socket;
    }

    public void run() {
	try {
	    System.out.println("New handler " + this 
			       + " for connection from " + socket);

	    // create read stream to get input
	    BufferedReader inFromClient = 
		new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    String clientSentence = inFromClient.readLine();

	    // process input
	    String capitalizedSentence = clientSentence.toUpperCase() + '\n';

	    // send reply
	    DataOutputStream outToClient = 
		new DataOutputStream(socket.getOutputStream());
	    outToClient.writeBytes(capitalizedSentence);

	    socket.close();

	    System.out.println("Handler " + this + " exits");
	} catch(Exception e) {
	    System.out.println("Processing request failed");
	}
    } // end of run

}