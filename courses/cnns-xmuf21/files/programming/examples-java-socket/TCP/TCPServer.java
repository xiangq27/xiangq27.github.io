/*
 *
 * tcpServer from Kurose and Ross
 *
 * Usage: java TCPServer [server port]
 */

import java.io.*;
import java.util.*;
import java.net.*;

public class TCPServer {

  	// decide if step
    static boolean step = true;

    public static void main(String[] args)throws Exception {

		// see if we do not use default server port
		int serverPort = 6789;
		if (args.length >= 1)
	    	serverPort = Integer.parseInt(args[0]);
	    
		// create server socket
		ServerSocket welcomeSocket = new ServerSocket(serverPort, 1);
		System.out.println("Server started; listening at " + serverPort);

		while (true){
	        	checkStep("check before call accept");
	
		    	// accept connection from connection queue
		    	Socket connectionSocket = welcomeSocket.accept();
		    	System.out.println("accepted connection from " + connectionSocket);
	
		    	checkStep("check before read");
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

		} // end of while (true)

    } // end of main()

    public static void checkStep(String msg) {
        if (step) {
			System.out.println(msg);
	   		Scanner scan = new Scanner(System.in);
	   		scan.nextLine();
        }
    } // end of check Step

} // end of class TCPServer
