package st.runtracker.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tengsun on 2/2/16.
 */
public class TimeUtil {

    public static String getDisplayDatetime(Date date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        return format.format(date);
    }

}
