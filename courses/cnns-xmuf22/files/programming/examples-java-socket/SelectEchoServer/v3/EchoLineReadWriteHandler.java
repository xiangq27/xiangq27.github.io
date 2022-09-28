import java.nio.*;
import java.nio.channels.*;
import java.io.IOException;

public class EchoLineReadWriteHandler implements IReadWriteHandler {

	private ByteBuffer inBuffer;
	private ByteBuffer outBuffer;

	private boolean requestComplete;
	private boolean responseReady;
	private boolean responseSent;
	private boolean channelClosed;

	private StringBuffer request;

	// private enum State {
	// READ_REQUEST, REQUEST_COMPLETE, GENERATING_RESPONSE, RESPONSE_READY,
	// RESPONSE_SENT
	// }
	// private State state;

	public EchoLineReadWriteHandler() {
		inBuffer = ByteBuffer.allocate(4096);
		outBuffer = ByteBuffer.allocate(4096);

		// initial state
		requestComplete = false;
		responseReady = false;
		responseSent = false;
		channelClosed = false;

		request = new StringBuffer(4096);
	}

	public int getInitOps() {
		return SelectionKey.OP_READ;
	}

	public void handleException() {
	}

	public void handleRead(SelectionKey key) throws IOException {

		// a connection is ready to be read
		Debug.DEBUG("->handleRead");

		if (requestComplete) { // this call should not happen, ignore
			return;
		}

		// process data
		processInBuffer(key);

		// update state
		updateState(key);

		Debug.DEBUG("handleRead->");

	} // end of handleRead

	private void updateState(SelectionKey key) throws IOException {

		Debug.DEBUG("->Update dispatcher.");

		if (channelClosed)
			return;

		/*
		 * if (responseSent) { Debug.DEBUG(
		 * "***Response sent; shutdown connection"); client.close();
		 * dispatcher.deregisterSelection(sk); channelClosed = true; return; }
		 */

		int nextState = key.interestOps();
		if (requestComplete) {
			nextState = nextState & ~SelectionKey.OP_READ;
			Debug.DEBUG("New state: -Read since request parsed complete");
		} else {
			nextState = nextState | SelectionKey.OP_READ;
			Debug.DEBUG("New state: +Read to continue to read");
		}

		if (responseReady) {

			if (!responseSent) {
				nextState = nextState | SelectionKey.OP_WRITE;
				Debug.DEBUG("New state: +Write since response ready but not done sent");
			} else {
				nextState = nextState & ~SelectionKey.OP_WRITE;
				Debug.DEBUG("New state: -Write since response ready and sent");
			}
		}

		key.interestOps(nextState);

	}

	public void handleWrite(SelectionKey key) throws IOException {
		Debug.DEBUG("->handleWrite");

		// process data
		SocketChannel client = (SocketChannel) key.channel();
		Debug.DEBUG("handleWrite: Write data to connection " + client + "; from buffer " + outBuffer);
		int writeBytes = client.write(outBuffer);
		Debug.DEBUG("handleWrite: write " + writeBytes + " bytes; after write " + outBuffer);

		if (responseReady && (outBuffer.remaining() == 0)) {
			responseSent = true;
			Debug.DEBUG("handleWrite: responseSent");
		}

		// update state
		updateState(key);

		// try {Thread.sleep(5000);} catch (InterruptedException e) {}
		Debug.DEBUG("handleWrite->");
	} // end of handleWrite

	private void processInBuffer(SelectionKey key) throws IOException {
		Debug.DEBUG("processInBuffer");
		SocketChannel client = (SocketChannel) key.channel();
		int readBytes = client.read(inBuffer);
		Debug.DEBUG("handleRead: Read data from connection " + client + " for " + readBytes + " byte(s); to buffer "
				+ inBuffer);

		if (readBytes == -1) { // end of stream
			requestComplete = true;

			Debug.DEBUG("handleRead: readBytes == -1");
		} else {
			inBuffer.flip(); // read input
			// outBuffer = ByteBuffer.allocate( inBuffer.remaining() );
			while (!requestComplete && inBuffer.hasRemaining() && request.length() < request.capacity()) {
				char ch = (char) inBuffer.get();
				Debug.DEBUG("Ch: " + ch);
				request.append(ch);
				if (ch == '\r' || ch == '\n') {
					requestComplete = true;
					// client.shutdownInput();
					Debug.DEBUG("handleRead: find terminating chars");
				} // end if
			} // end of while
		}

		inBuffer.clear(); // we do not keep things in the inBuffer

		if (requestComplete) {
			generateResponse();
		}

	} // end of process input

	private void generateResponse() {
		for (int i = 0; i < request.length(); i++) {
			char ch = (char) request.charAt(i);

			ch = Character.toUpperCase(ch);

			outBuffer.put((byte) ch);
		}
		outBuffer.flip();
		responseReady = true;
	} // end of generate response

}