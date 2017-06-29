public class ParseHTTPHeader {
	
	/**
	 * Parse a HTTP Header.
	 * @param s, the string to parse.
	 * @param type, returns in the first element in a 1-D array the type,
	 * 		  if HTTP, then returns 1. if HTTPS, then 2, else 0.
	 * @param url, returns the requested url in the passed in stringbuilder.
	 * @param portnum, from which port the user sent the HTTP request.
	 * @param firstLine, the first line of the request
	 * @return the replaced header to send to the server.
	 */
	public static String ReplaceHeader(char[] s, int[] type, StringBuilder url, int[] portnum, StringBuilder firstLine) {
		String str = new String(s);
		if (str.substring(0, 4).equals("GET ") /*|| str.substring(0, 5).equals("POST ")*/) {
			// We just set some of the output parameters, then return the new header
			// where we replace any HTTP/... with HTTP/1.0 and then match any
			// Connection: message with Connection:close.
			type[0] = 1;
			firstLine.append(str.substring(0,str.indexOf(" HTTP/")));
			str = str.replaceFirst("HTTP/...", "HTTP/1.0");
			str = str.replaceFirst("Connection: .*(\r\n|\r|\n)", "");
			int k = str.indexOf("Host: ");
			int endSeq1 = str.substring(k).indexOf("\r") + k;
			int endSeq2 = str.substring(k).indexOf("\n") + k;
			int endSeq = Integer.min(endSeq1, endSeq2);
			url.append((str.substring(k+6, endSeq)));
			int end = str.indexOf("\r\n\r\n");
			return str.substring(0, end) + "\r\nConnection: close\r\n\r\n";
		} else if (str.substring(0,8).equals("CONNECT ")) {
			// Else for CONNECT: set the output parameters and get the port
			// to connect to.
			int k = str.indexOf("Host: ");
			String j = str.substring(k+6);
			url.append((j.substring(0, j.indexOf(":"))));
			type[0] = 2;
			String p = str.substring(str.indexOf(":") + 1);
			portnum[0] = Integer.valueOf(p.substring(0, p.indexOf(" ")));
			return str;
		} else {
			type[0] = 0;
			return "BAD";
		}
	}
	
}
