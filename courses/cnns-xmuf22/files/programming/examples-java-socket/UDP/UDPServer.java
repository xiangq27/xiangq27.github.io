
/*
 *
 * UDPServer from Kurose and Ross, revised by YRY
 *
 * Usage: java UDPServer [server port]
 */
import java.util.*;
import java.net.*;

public class UDPServer {

	// default server port
	private static int serverPort = 9876;

	// decide if step: process one datagram at a time
	private static boolean step = true;

	public static void main(String[] args) throws Exception {

		// see if we do not use default server port
		if (args.length >= 1)
			serverPort = Integer.parseInt(args[0]);

		//create server datagram socket
		DatagramSocket serverSocket = new DatagramSocket(serverPort);

		//InetAddress sIP1 = InetAddress.getByName("localhost");
		//DatagramSocket serverSocket = new DatagramSocket(serverPort, sIP1);

		//InetAddress sIP2 = InetAddress.getByName("10.31.167.104");
		//DatagramSocket serverSocket = new DatagramSocket(serverPort, sIP2);

		System.out.println("Server listening port = " + serverPort);

		sockStat(serverSocket);

		checkStep("Press enter to start receiving msgs...");

		byte[] receiveData = new byte[1024];
		byte[] sendData = new byte[1024];

		while (true) {

			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

			serverSocket.receive(receivePacket);

			// process data
			String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
			String capitalizedSentence = sentence.toUpperCase();
			sendData = capitalizedSentence.getBytes();

			// determine client address and port
			InetAddress cliIPAddress = receivePacket.getAddress();
			int cliPort = receivePacket.getPort();

			// construct datagram and then send
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, cliIPAddress, cliPort);
			serverSocket.send(sendPacket);

			checkStep("Press enter to receive next msg...");
		} // end of while

	} // end of main

	public static void sockStat(DatagramSocket sock) throws Exception {
		System.out.println("Local addr: " + sock.getLocalAddress() + ":" + sock.getLocalPort());

		System.out.println("Buffer size: snd = " + sock.getSendBufferSize() + "; rcv = " + sock.getReceiveBufferSize());

		System.out.println("Socket TO: " + sock.getSoTimeout());

		System.out.println("Traffic class: " + sock.getTrafficClass());

		// System.out.println( "Charset: " +
		// java.nio.charset.Charset.defaultCharset() );

	} // end of sockStat

	public static void checkStep(String msg) {
		if (step) {
			System.out.println(msg);
			Scanner scan = new Scanner(System.in);
			scan.nextLine();
		}
	} // end of check Step

} // end of UDPServer
