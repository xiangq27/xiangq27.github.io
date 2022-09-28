import java.nio.channels.*;
import java.io.IOException;

public class Acceptor implements IAcceptHandler {

	private ISocketReadWriteHandlerFactory srwf;

	public Acceptor(ISocketReadWriteHandlerFactory srwf) {
		this.srwf = srwf;
	}

	public void handleException() {
		System.out.println("handleException(): of Acceptor");
	}

	public void handleAccept(SelectionKey key) throws IOException {
		ServerSocketChannel server = (ServerSocketChannel) key.channel();

		// extract the ready connection
		SocketChannel client = server.accept();
		Debug.DEBUG("handleAccept: Accepted connection from " + client);

		// configure the connection to be non-blocking
		client.configureBlocking(false);

		/*
		 * register the new connection with *read* events/operations
		 * SelectionKey clientKey = client.register( selector,
		 * SelectionKey.OP_READ);// | SelectionKey.OP_WRITE);
		 */

		IReadWriteHandler rwH = srwf.createHandler();
		int ops = rwH.getInitOps();

		SelectionKey clientKey = client.register(key.selector(), ops);
		clientKey.attach(rwH);

	} // end of handleAccept

} // end of class