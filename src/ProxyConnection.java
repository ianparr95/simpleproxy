import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ProxyConnection implements Runnable {
	
	// connection to the client
	private Socket clientConnection;
	// connection to the website
	private Socket urlConnection;
	// reading in from client
	private BufferedReader inFromClient;
	// sending out to the client
	private OutputStream outToClient;
	
	/**
	 * Sets up a new ProxyConnection with the specified client socket.
	 * @param cSocket, the socket to the client
	 * @param timeout, the socket timeout, in milliseconds
	 * @throws IOException
	 */
	public ProxyConnection(Socket cSocket, int timeout) throws IOException {
		this.clientConnection = cSocket;
		inFromClient = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
		outToClient = clientConnection.getOutputStream();
		urlConnection = new Socket();
		//cSocket.setSoTimeout(timeout);
	}

	@Override
	public void run() {
		// Wait for user to input data firstly:
		char[] inBuf = new char[65536];
		try {
			// Parse header.
			int k = 0;
			while (true) {
				k = inFromClient.read(inBuf);
				// Read until no more left or we reach the end of the header
				if (k == -1) {
					break;
				}
				if ((k > 4 && inBuf[k - 1] == '\n' && inBuf[k - 2] == '\r' && inBuf[k - 3] == '\n'
						&& inBuf[k - 4] == '\r'))
					break;
			}

			// Fix the header by changing all the requests to be HTTP 1.0,
			// and also to set them to be connection: closed.
			int type[] = new int[1];
			int portnum[] = new int[1];
			type[0] = -1;
			StringBuilder url2 = new StringBuilder();
			StringBuilder firstLine = new StringBuilder();
			// We get the header and return the url, portnum, firstline, and type of message:
			String fixedHeader = ParseHTTPHeader.ReplaceHeader(inBuf, type, url2, portnum, firstLine);
			String url = url2.toString();
			if (type[0] != 1 && type[0] != 2) {
				// bad request: do nothing.
			} else if (type[0] == 1) {
				// Request is type HTTP: we just print out the log, create a new connection to the url at port 80
				// then start the HTTPConnection
				try {
					urlConnection.connect(new InetSocketAddress(InetAddress.getByName(url), 80));
				} catch (Exception e) {
					// Failed to connect: just send back a fail, no need to log anything.
					String h = "HTTP 502 Bad Gateway\r\n\r\n";
					outToClient.write(h.getBytes());
					clientConnection.close();
					urlConnection.close();
					return;
				}
				System.out.println(GetCurrentDate.getCurrentDate() + " - >>> " + firstLine.toString());
				HTTPConnection hc = new HTTPConnection(outToClient, urlConnection.getOutputStream(), urlConnection.getInputStream(), fixedHeader);
				hc.start();
				//hc.start is blocking, so when it ends then we close the sockets.
				clientConnection.close();
				urlConnection.close();
			} else {
				// HTTPS connection
				try {
					urlConnection.connect(new InetSocketAddress(InetAddress.getByName(url), portnum[0]));
				} catch (Exception e) {
					String h = "HTTP 502 Bad Gateway\r\n\r\n";
					outToClient.write(h.getBytes());
					clientConnection.close();
					urlConnection.close();
					return;
				}
				// just print out the log then create a new HTTPSConnection
				System.out.println(GetCurrentDate.getCurrentDate() + " - >>> CONNECT " + url + ":" + portnum[0]);
				HTTPSConnection hc = new HTTPSConnection(clientConnection, urlConnection);
				// start the HTTPSConnection
				hc.start();
				// When done: just close connections.
				clientConnection.close();
				urlConnection.close();
			}
		} catch (Exception e) {
			// If any exception occurs, just force closing the connections.
			try {
				clientConnection.close();
			} catch (IOException e1) {
			}
			try {
				urlConnection.close();
			} catch (IOException e1) {
			}
		}
	}

}
