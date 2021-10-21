/* 
 *
 * XMU CNNS Class Demo
 */
import java.io.*;
import java.util.*;
import java.net.*;

public class Server {
  
    private ServerSocket welcomeSocket;
	
    public final static int THREAD_COUNT = 2;
    private ServiceThread[] threads;

    /* Constructor: starting all threads at once */
    public Server(int serverPort) {

	    try {
	        // create server socket
	        welcomeSocket = new ServerSocket(serverPort);
	        System.out.println("Server started; listening at " + serverPort);

	        // create thread pool
	        threads = new ServiceThread[THREAD_COUNT];

	        // start all threads
	        for (int i = 0; i < threads.length; i++) {
			    threads[i] = new ServiceThread(welcomeSocket); 
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

    // Wait for all threads to finish
    public void run() {

	    try {
	        for (int i = 0; i < threads.length; i++) {
			    threads[i].join();
	        }
	        System.out.println("All threads finished. Exit");
	    } catch (Exception e) {
	        System.out.println("Join errors");
	    } // end of catch
			
    } // end of run

} // end of class
