/*
 *
 * UDPClient from Kurose and Ross
 *
 * Usage: java UDPClient [server addr] [server port]
 */

import java.io.*;
import java.net.*;

public class UDPClient2 {

    public static void main(String[] args) throws Exception {

	// get server address
	String serverName = "localhost";
	if (args.length >= 1)
	    serverName = args[0];
	InetAddress serverIPAddress = InetAddress.getByName(serverName);

	// get server port
	int serverPort = 9876;
	if (args.length >= 2)
	    serverPort = Integer.parseInt(args[1]);

	// create socket
	DatagramSocket clientSocket = new DatagramSocket();
        sockStat( clientSocket );
	
	// get input from keyboard
	BufferedReader inFromUser = new BufferedReader(new InputStreamReader (System.in));
	System.out.print("Message: ");
	String sentence = inFromUser.readLine();

	byte[] sendData = new byte[1024];
	sendData = sentence.getBytes("UTF-8");
	System.out.println("[" + sendData.length + "]" ); 

	// construct and send datagram
	DatagramPacket sendPacket = new DatagramPacket(sendData, 
						       sendData.length, 
						       serverIPAddress, 
						       serverPort);
	clientSocket.send(sendPacket);

	// receive datagram
	byte[] receiveData = new byte [1024];
	DatagramPacket receivePacket = new DatagramPacket(receiveData, 
							  receiveData.length);
	clientSocket.receive(receivePacket);

	// print output
	String sentenceFromServer = new String(receivePacket.getData(), "UTF-8");
	System.out.println("From Server: " + sentenceFromServer);

	// close client socket
	clientSocket.close();

    } // end of main

    public static void sockStat(DatagramSocket sock) throws Exception {
	System.out.println( "Rcv buffer Size: " + sock.getReceiveBufferSize() );
	System.out.println( "Snd buffer size: " + sock.getSendBufferSize() );
	System.out.println( "Socket TO: " + sock.getSoTimeout() );

	System.out.println( "Charset: " + java.nio.charset.Charset.defaultCharset() );

    }

} // end of UDPClient
