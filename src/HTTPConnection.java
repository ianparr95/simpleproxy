import java.io.InputStream;
import java.io.OutputStream;

public class HTTPConnection {
	// Out to server
	private OutputStream ots;
	// In from server
	private InputStream is;
	// Out to client.
	private OutputStream otc;
	// The initial header to send that was converted to be a HTTP 1.0 connection closed one.
	private String fixedHeader;

	/**
	 * Instantiates a new HTTP 1.0 connection.
	 * @param otc, output stream to the client.
	 * @param ots, output stream to send to the server.
	 * @param is, input stream, reading from the server.
	 * @param head, HTTP 1.0 Header to send to the server.
	 */
	public HTTPConnection(OutputStream otc, OutputStream ots, InputStream is, String head) {
		this.is = is;
		this.ots = ots;
		this.fixedHeader = head;
		this.otc = otc;
	}

	/**
	 * Start a new instance of this HTTPConnection.
	 * Should be run multi-threaded.
	 * @throws Multiple exceptions, related to connections.
	 */
	public void start() throws Exception {
		int k = 0;
		try {
			// Send the HTTP 1.0 header initially.
			ots.write(fixedHeader.getBytes());
			byte[] bbuf = new byte[65536];
			while (true) {
				// Read from in from server to bbuf
				try{
					k = is.read(bbuf);
				} catch (Exception e) {
					is.close();
					ots.close();
					otc.close();
					break;
				}
				// Write what server sends to us back to client.
				if (k > 0){
					otc.write(bbuf, 0, k);
				} else {
					break;
				}
			}
			is.close();
			ots.close();
			otc.close();
		} catch (Exception e) {
			is.close();
			ots.close();
			otc.close();
		}
	}
	
}
