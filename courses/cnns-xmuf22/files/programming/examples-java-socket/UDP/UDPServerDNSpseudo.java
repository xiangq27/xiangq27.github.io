/*
 *
 * UDPServer from Kurose and Ross
 *
 * Usage: java UDPServer [server port]
 */
import java.util.*;
import java.net.*;

public class UDPServerDNSpseudo {

	public static void main(String[] args) throws Exception {

		// see if we do not use default server port
		int serverPort = 53;
		if (args.length >= 1)
			serverPort = Integer.parseInt(args[0]);
		
		// create server datagram socket
		DatagramSocket serverSocket = new DatagramSocket(serverPort);

		System.out.println( "Server listening port = " + serverPort );
		sockStat(serverSocket);
		
		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];

		while (true){

			DatagramPacket receivePacket 
			= new DatagramPacket (receiveData, receiveData.length);

			serverSocket.receive(receivePacket);

			/*
			  if query
			     foreach question
			       check if there is answer in cache
			       else // not in cache
			         generate DNS msg ...
			  else reply
                               update cache
			       xfxuxs
			 */
			// process data
			//String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
			//String capitalizedSentence = sentence.toUpperCase();
			//sendData = capitalizedSentence.getBytes();

			// determine client address and port
			InetAddress cliIPAddress = receivePacket.getAddress();
			int cliPort = receivePacket.getPort();

			// construct datagram and then send
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, cliIPAddress, cliPort);
			serverSocket.send(sendPacket);
		} // end of while

	} // end of main

	public static void sockStat(DatagramSocket sock) throws Exception {
		System.out.println( "Local addr: " + sock.getLocalAddress() + ":" + sock.getLocalPort());
		
		System.out.println( "Buffer size: snd = " + sock.getSendBufferSize() + "; rcv = " + sock.getReceiveBufferSize());		
		System.out.println( "Socket TO: " + sock.getSoTimeout() );

	}
} // end of UDPServer

//System.out.println( "Charset: " + java.nio.charset.Charset.defaultCharset() );
