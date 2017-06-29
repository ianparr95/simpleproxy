import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Proxy {
	
	public static void main(String args[]) throws IOException, InterruptedException {
		int portnum = 0;
		boolean debug = true;
		if (!debug) {
		String port = args[0];
		// parse the port string
		try {
			portnum = Integer.parseInt(port);
		} catch (Exception e) {
			// If user entered not a number
			System.err.println("Please enter a valid port number...");
			System.exit(1);
		}
		if (portnum < 0 || portnum > 65536) {
			System.err.println("Please enter a valid port number...");
			System.exit(1);
		}
		} else {
			portnum = 14561;
		}
		// server socket to listen to new incoming connections
		ServerSocket ss = new ServerSocket(portnum, 500);
		// Console thread, so just run program until user types ctrl + c to exit.
		(new Thread(new Runnable(){
			@Override
			public void run() {
				while (true) {
					BufferedReader s = null;
					try {
						s = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// Shouldn't be reached...
						e.printStackTrace();
						System.exit(0);
					}
					try {
						String curLine = s.readLine();
						// If console reached eof: just close and exit
						if (curLine == null) {
							ss.close();
							System.exit(0);
						}
					} catch (IOException e) {
						System.err.println("Some fatal exception occured with the console.. exiting");
						System.exit(1);
					}
				}
			}
		})).start();
		
		System.out.println(GetCurrentDate.getCurrentDate() + " - Proxy listenining on " + portnum);
		ExecutorService pool = Executors.newFixedThreadPool(500); // all proxy threads

		while (true) {
			try {
				// Accept new connections
				Socket newConnection = ss.accept();
				//start a new ProxyConnection thread.
				pool.execute(new ProxyConnection(newConnection, 5000));
			} catch (Exception e) {
				// This can occur when we reaching eof on system.in
				// so just thread.sleep firstly so that we close from the other thread instead.
				Thread.sleep(1000);
				ss.close();
				System.exit(1);
			}
		}
	}
}
