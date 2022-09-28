/*
 *
 *  client for TCPClient from Kurose and Ross
 *
 *  * Usage: java TCPClient [server addr] [server port]
 */
import java.io.*;
import java.net.*;

public class TCPClient {

	public static void main(String[] args) throws Exception {

		// get server address
		String serverName = "localhost";
		if (args.length >= 1)
		    serverName = args[0];
		InetAddress serverIPAddress = InetAddress.getByName(serverName);

		// get server port
		int serverPort = 6789;
		if (args.length >= 2)
		    serverPort = Integer.parseInt(args[1]);

		// create socket which connects to server
		Socket clientSocket = new Socket(serverIPAddress, serverPort);

        System.out.println("client sock created; waiting for keyboard...");

		// get input from keyboard
		BufferedReader inFromUser =
			new BufferedReader(new InputStreamReader(System.in));
		String sentence = inFromUser.readLine();

		// write to server
		DataOutputStream outToServer 
		   = new DataOutputStream(clientSocket.getOutputStream());
		outToServer.writeBytes(sentence + '\n');

        //BufferedOutputStream bos = new BufferedOutputStream( clientSocket.getOutputStream() );
		//DataOutputStream outToServer = new DataOutputStream( bos );
		//outToServer.writeBytes(sentence + '\n');

		// outToServer.flush();

        System.out.println("written to server; waiting for server reply...");

		// create read stream and receive from server
		BufferedReader inFromServer 
		 = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		String sentenceFromServer = inFromServer.readLine();

		// print output
		System.out.println("From Server: " + sentenceFromServer);

		// close client socket
		clientSocket.close();

	} // end of main

} // end of class TCPClient
