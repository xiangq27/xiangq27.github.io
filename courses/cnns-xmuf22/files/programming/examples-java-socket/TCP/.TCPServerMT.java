/*
 *
 * CS433/533
 *
 * Usage: java TCPServerMT [server port]
 */

import java.io.*;
import java.net.*;

public class TCPServerMT {

	public static void main(String[] args)throws Exception {

		// see if we do not use default server port
		int serverPort = 6789;
		if (args.length >= 1)
		    serverPort = Integer.parseInt(args[0]);
	    
		// create server socket
		ServerSocket welcomeSocket = new ServerSocket(serverPort);
		System.out.println("Server started; listening at " + serverPort);

		while (true){

		    // accept connection from connection queue
		    Socket connectionSocket = welcomeSocket.accept();

			RequestHandler r = new RequestHandler(connectionSocket);

			Thread t = new Thread(r);
			t.start();

		} // end of while (true)

	} // end of main()

} // end of class TCPServer


