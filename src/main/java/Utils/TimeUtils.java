package Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {

	/**
	 * This method gets a String timestamp in the format h:mm:ss.
	 * @return A String.
	 */
	public static String getTimeStamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm:ss a");
		return sdf.format(date);
	}
}
