import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HTTPSConnection{
	
	// out to client
	private OutputStream otc;
	// out to server
	private OutputStream ots;
	// in from client
	private InputStream ic;
	// in from server
	private InputStream is;
	
	/**
	 * Instantiate a new HTTPS Connection
	 * @param client, the socket to the client
	 * @param server, the socket to the server
	 * @throws IOException
	 */
	public HTTPSConnection(Socket client, Socket server) throws IOException {
		otc = client.getOutputStream();
		ots = server.getOutputStream();
		ic = client.getInputStream();
		is = server.getInputStream();
	}
	
	/**
	 * Start this connection.
	 */
	public void start() {
		try {
			// Write the initial ok message back to client.
			String h = "HTTP 200 OK\r\n\r\n";
			otc.write(h.getBytes());
			// Create new read/write instances for client to server
			// and server to client, and invoke these new threads
			rwInstance clientToServer = new rwInstance(ic, ots);
			new Thread(clientToServer).start();
			rwInstance serverToClient = new rwInstance(is, otc);
			new Thread(serverToClient).start();
			// While these threads aren't closed, we just keep this thread alive
			while(!serverToClient.isClosed || !clientToServer.isClosed){Thread.sleep(100);}
			// When one is closed, we want to close this connection, which we do when we return
			// but first just close the streams.
			otc.close();
			ots.close();
			ic.close();
			is.close();
		} catch (Exception e) {
			try {
				otc.close();
				ots.close();
				ic.close();
				is.close();
			}catch (Exception e2){//shouldn't be reached...
				
			}
		}
	}

	// This class just lets us run a new thread of an instance
	// that reads from one stream then writes it to another.
	private static class rwInstance implements Runnable{

		InputStream is;
		OutputStream os;
		volatile boolean isClosed = false;
		
		/**
		 * Instantiate a new instance of this class
		 * @param is, the input stream to read from.
		 * @param os, the output stream to write to.
		 */
		rwInstance(InputStream is, OutputStream os) {
			this.is = is;
			this.os = os;
		}
		
		@Override
		public void run() {
			try {
				byte[] cbuf = new byte[65536];
				int j;
				while ((j = is.read(cbuf)) > 0){
					os.write(cbuf, 0, j);
				}
				isClosed = true;
			} catch (Exception e) {
				isClosed = true;
			}
			
		}

	}
	
}