package io.odysz.antson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**Date formatting and parsing helper.
 * @author ody */
public class DateFormat {
	/**yyyy-MM-dd or %Y-%M-%e*/
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	/**yyyy-MM-dd-hhmmss or %Y-%M-%e ...*/
	public static SimpleDateFormat sdflong_sqlite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
	// public static SimpleDateFormat sdflong_mysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**yyyy-MM-dd
	 * @param d
	 * @return formatted string
	 */
	static public String format(Date d) { return d == null ? " - - " : sdf.format(d); }

	/**yyyy-MM-dd
	 * @param text
	 * @return formatted string
	 * @throws ParseException
	 */
	public static Date parse(String text) throws ParseException { return sdf.parse(text); }

//	public static String incSeconds(dbtype drvType, String date0, int snds) throws ParseException {
//		Date d0 = parse(date0);
//		d0.setTime(d0.getTime() + snds);
//		// return format(d0);
//		if (drvType == dbtype.sqlite)
//			return sdflong_sqlite.format(d0);
//		return sdflong_mysql.format(d0);
//	}


	public static String getDayDiff(Date date2, Date date1) {
		if (date2 == null || date1 == null)
			return "-";
		return String.valueOf(getDayDiffInt(date2, date1));
	}

	public static long getDayDiffInt(Date d2, Date d1) {
			if (d2 == null || d1 == null)
			return -1;
		long diff = d2.getTime() - d1.getTime();
		return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
	}

	public static String getTimeStampYMDHms() {
		Date now = new Date();
		return sdflong_sqlite.format(now);
	}
}
