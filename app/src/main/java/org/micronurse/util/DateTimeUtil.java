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
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd HH:mm:ss");
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @SuppressWarnings("deprecation")
    public static String convertTimestamp(Context context, long timestamp){
        Date currentDate = new Date();
        Date specificDate = new Date(timestamp);
        if(specificDate.getYear() == currentDate.getYear() && specificDate.getMonth() == currentDate.getMonth()
                && specificDate.getDate() == currentDate.getDate()) {
            return context.getString(R.string.today) + ' ' + sdf.format(specificDate);
        }else if(specificDate.getYear() == currentDate.getYear() && specificDate.getMonth() == currentDate.getMonth()
                && specificDate.getDate() == currentDate.getDate() - 1){
            return context.getString(R.string.yesterday) + ' ' + sdf.format(specificDate);
        }else if(specificDate.getYear() == currentDate.getYear()){
            return sdf2.format(specificDate);
        }
        return sdf3.format(specificDate);
    }
}
