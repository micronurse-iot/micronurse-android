package org.micronurse.util;

import android.annotation.SuppressLint;
import android.content.Context;

import org.micronurse.R;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zhou-shengyun on 8/15/16.
 */
public class DateTimeUtil {
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd");

    public static String convertTimestamp(Context context, Date time, boolean showDate, boolean showTime){
        Date currentDate = new Date();
        String timeStr = "";
        if(time.getYear() == currentDate.getYear() && time.getMonth() == currentDate.getMonth()
                && time.getDate() == currentDate.getDate()) {
            if(showDate && !showTime)
                timeStr = context.getString(R.string.today);
        }else if(time.getYear() == currentDate.getYear() && time.getMonth() == currentDate.getMonth()
                && time.getDate() == currentDate.getDate() - 1){
            if(showDate) {
                timeStr = context.getString(R.string.yesterday);
                if(showTime)
                    timeStr += ' ';
            }
        }else if(time.getYear() == currentDate.getYear()){
            if(showDate) {
                timeStr = sdf2.format(time);
                if(showTime)
                    timeStr += ' ';
            }
        }else if(showDate){
            timeStr = sdf3.format(time);
            if(showTime)
                timeStr += ' ';
        }
        if(showTime)
            timeStr += sdf.format(time);
        return timeStr;
    }

    @SuppressWarnings("deprecation")
    public static String convertTimestamp(Context context, long timestamp){
        return convertTimestamp(context, new Date(timestamp), true, true);
    }

    public static String convertTimestamp(Context context, long timestamp, boolean showDate, boolean showTime){
        return convertTimestamp(context, new Date(timestamp), showDate, showTime);
    }

    @SuppressWarnings("deprecation")
    public static boolean isSameDay(Date d1, Date d2){
        return d1.getYear() == d2.getYear() && d1.getMonth() == d2.getMonth()
                && d1.getDate() == d2.getDate();
    }
}
