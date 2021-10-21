/**
 ** XMU CNNS Class Demo WebRequestHandler
 **/


import java.io.*;
import java.net.*;
import java.util.*;

class WebRequestHandler implements Runnable {

	String WWW_ROOT;
	Socket connSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;

	String urlName;
	String fileName;
	File fileInfo;

	public WebRequestHandler(Socket connectionSocket, 
							 String WWW_ROOT) throws Exception
	{
		this.WWW_ROOT = WWW_ROOT;
		this.connSocket = connectionSocket;

		inFromClient =
		    new BufferedReader(new InputStreamReader(connSocket.getInputStream()));

		outToClient =
			new DataOutputStream(connSocket.getOutputStream());

	}

	public void run() 
	{
		processRequest();
	}

	public void processRequest() 
	{

		try {
			mapURL2File();

			if ( fileInfo != null ) // found the file and knows its info
			{
				outputResponseHeader();
				outputResponseBody();
			} 

			connSocket.close();
		} catch (Exception e) {
			outputError(400, "Server error");
		}



    } // end of processARequest

	private void mapURL2File() throws Exception 
	{
				
		String requestMessageLine = inFromClient.readLine();
		System.out.println("request line: " + requestMessageLine);

		// process the request
		String[] request = requestMessageLine.split("\\s");
		
		if (request.length < 2 || !request[0].equals("GET"))
		{
			outputError(500, "Bad request");
			return;
		}

		// parse URL to retrieve file name
		urlName = request[1];
	    
		if ( urlName.startsWith("/") == true )
			urlName  = urlName.substring(1);

		// map to file name
		fileName = WWW_ROOT + urlName;
		System.out.println ("Map to File name: " + fileName);

		fileInfo = new File( fileName );
		if ( !fileInfo.isFile() ) 
		{
			outputError(404,  "Not Found");
			fileInfo = null;
		}

	} // end mapURL2file


    private void outputResponseHeader() throws Exception 
    {
		outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
	
		if (urlName.endsWith(".jpg"))
			outToClient.writeBytes("Content-Type: image/jpeg\r\n");
		else if (urlName.endsWith(".gif"))
			outToClient.writeBytes("Content-Type: image/gif\r\n");
		else if (urlName.endsWith(".html") || urlName.endsWith(".htm"))
			outToClient.writeBytes("Content-Type: text/html\r\n");
		else
			outToClient.writeBytes("Content-Type: text/plain\r\n");
    }

    private void outputResponseBody() throws Exception 
    {

		int numOfBytes = (int) fileInfo.length();
		outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
		outToClient.writeBytes("\r\n");
	
		// send file content
		FileInputStream fileStream  = new FileInputStream (fileName);
	
		byte[] fileInBytes = new byte[numOfBytes];
		fileStream.read(fileInBytes);

		outToClient.write(fileInBytes, 0, numOfBytes);
	}

    void outputError(int errCode, String errMsg)
    {
		try {
		outToClient.writeBytes("HTTP/1.0 " + errCode + " " + errMsg + "\r\n");
		} catch (Exception e) {}
	}
}
