/*
 * Copyright 2013-2018 Lilinfeng.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author lilinfeng
 * @version 1.0
 */
public class TimeServer {

    /**
     * @param args
     * @throws IOException
     */
    public static  void main(String[] args) throws IOException {
		int port = 8080;
		if (args != null && args.length > 0) {

	    	try {
				port = Integer.valueOf(args[0]);
	    	} catch (NumberFormatException e) {
	    	}
		}

		ServerSocket server = null;
		try {
	    	server = new ServerSocket(port);
	    	System.out.println("Time server listens at port: " + port);	    	

			// Create Java Executor Pool
	    	TimeServerHandlerExecutePool myExecutor 
	    			= new TimeServerHandlerExecutePool(50, 10000);

	    	Socket socket = null;
	    	while (true) {
				socket = server.accept();
				myExecutor.execute(new TimeServerHandler(socket));
	    	} // end of while

		} finally {
	    	if (server != null) {
				System.out.println("The time server closes.");
				server.close();
				server = null;
	    	}
		} // end of finally
    } // end of main
}