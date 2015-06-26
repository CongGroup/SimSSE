package tool;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by HarryC on 8/5/14.
 *
 * This class is used to implement several common function related to time and date.
 */
public class TimeUtil {

    public static final String TIME_FORMAT_YMD_HMS = "YYYY-MM-dd hh:mm:ss";

    public static final String TIME_FORMAT_YMD = "YYYY-MM-dd";

    public static final String TIME_FORMAT_HMS_12 = "hh:mm:ss";

    public static final String TIME_FORMAT_HMS_24 = "HH:mm:ss";


    public static String timeToString(Calendar calendar, String timeFormat) {
        SimpleDateFormat df = new SimpleDateFormat(timeFormat);
        return df.format(calendar.getTime());
    }
}
