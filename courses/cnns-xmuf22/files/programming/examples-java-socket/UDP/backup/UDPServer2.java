/*
 *
 * UDPServer from Kurose and Ross
 *
 * Usage: java UDPServer [server port]
 */
import java.util.*;
import java.net.*;

public class UDPServer2 {

    public static void main(String[] args) throws Exception {

	// see if we do not use default server port
	int serverPort = 9876;
	if (args.length >= 1)
	    serverPort = Integer.parseInt(args[0]);
	    
	// create server datagram socket
	DatagramSocket serverSocket = new DatagramSocket(serverPort);

	System.out.println( "Server listening port = " + serverPort );

	byte[] receiveData = new byte[1024];
	byte[] sendData = new byte[1024];

	while (true){

	    DatagramPacket receivePacket 
		= new DatagramPacket (receiveData, receiveData.length);

	    //System.out.println("Press enter to receive next request");
	    //Scanner scan = new Scanner(System.in);
	    //scan.nextLine();

	    serverSocket.receive(receivePacket);

	    // process data
	    String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength(), "UTF-8");
	    String capitalizedSentence = sentence.toUpperCase();
	    sendData = capitalizedSentence.getBytes("UTF-8");

	    // determine client address and port
	    InetAddress cliIPAddress = receivePacket.getAddress();
	    int cliPort = receivePacket.getPort();

	    // construct datagram
	    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, cliIPAddress, cliPort);
	    // send
	    serverSocket.send(sendPacket);
	} // end of while

    } // end of main

} // end of UDPServer
