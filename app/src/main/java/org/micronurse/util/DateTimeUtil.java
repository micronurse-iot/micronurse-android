package org.micronurse.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhou-shengyun on 8/15/16.
 */
public class DateTimeUtil {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String convertTimestamp(long timestamp){
        return sdf.format(new Date(timestamp));
    }
}
