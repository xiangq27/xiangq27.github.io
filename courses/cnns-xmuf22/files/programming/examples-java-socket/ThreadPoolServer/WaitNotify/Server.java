/* 
 * 
 * XMU CNNS Class Demo
 */
import java.io.*;
import java.util.*;
import java.net.*;

public class Server {
  
	private ServerSocket welcomeSocket;
	
	public final static int THREAD_COUNT = 3;
    private ServiceThread[] threads;
	private List<Socket> connSockPool;

	/* Constructor: starting all threads at once */
	public Server(int serverPort) {

		try {
			// create server socket
			welcomeSocket = new ServerSocket(serverPort);
			System.out.println("Server started; listening at " + serverPort);

			connSockPool = new Vector<Socket>();

			// create thread pool
			threads = new ServiceThread[THREAD_COUNT];

			// start all threads
			for (int i = 0; i < threads.length; i++) {
				threads[i] = new ServiceThread(connSockPool); 
				threads[i].start();
			}
		} catch (Exception e) {
			System.out.println("Server construction failed.");
		} // end of catch

	} // end of Server

	public static void main(String[] args) {
		// see if we do not use default server port
		int serverPort = 6789;
		if (args.length >= 1)
		    serverPort = Integer.parseInt(args[0]);

		Server server = new Server(serverPort);
		server.run();

	} // end of main

	// 
	public void run() {

		while (true) {
			try {
				// accept connection from connection queue
				Socket connSock = welcomeSocket.accept();
				System.out.println("Main thread retrieve connection from " 
								   + connSock);

				// how to assign to an idle thread?
				synchronized (connSockPool) {
					connSockPool.add(connSock);
					connSockPool.notifyAll();
				} // end of sync
			} catch (Exception e) {
				System.out.println("Accept thread failed.");
			} // end of catch
		} // end of while

	} // end of run

} // end of class
