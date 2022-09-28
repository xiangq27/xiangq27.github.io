import java.nio.channels.SelectionKey;
import java.io.IOException;

public interface IReadWriteHandler extends IChannelHandler {
	
	public void handleRead(SelectionKey key) throws IOException;

	public void handleWrite(SelectionKey key) throws IOException;

	public int getInitOps();
}