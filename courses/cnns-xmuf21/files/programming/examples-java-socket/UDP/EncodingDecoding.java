
/*
 *
 * EncodingDecoding.java
 *
 * Usage: java EncodingDecoding <src charset> <dst charset>
 */

import java.io.*;
import java.net.*;

public class EncodingDecoding {

	public static void main(String[] args) throws IOException {

		if (args.length != 2) {
			System.out.println("Usage: java EncodingDecoding <src charset> <dst charset");
			System.exit(1);
		}

		// get input from keyboard
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Original message: ");
		String sentence = inFromUser.readLine();

		// Encoding
		byte[] sendData = sentence.getBytes(args[0]);

		// Decoding
		String rcvData = new String(sendData, args[1]);
		String capitalizedSentence = rcvData.toUpperCase();
		System.out.println("After transformation: " + capitalizedSentence);

	} // end of main

} // end of EncodingDecoding
