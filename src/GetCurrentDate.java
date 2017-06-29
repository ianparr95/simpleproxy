import java.text.SimpleDateFormat;
import java.util.Date;

public class GetCurrentDate {
	
	private static SimpleDateFormat format = new SimpleDateFormat("dd MMM HH:mm:ss");
	
	/**
	 * Returns a string in the format
	 * dd MMM HH:mm:ss of the current date/time
	 * @return a string of the current date and time.
	 */
	public static String getCurrentDate() {
		return format.format(new Date());
	}
}
