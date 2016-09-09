package org.micronurse.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import org.micronurse.R;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zhou-shengyun on 9/7/16.
 */
public class MonitorWarningReceiver extends BroadcastReceiver {
    public static final int MONITOR_WARNING_NOTIFICATION_ID = 34985723;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String title = bundle.getString(JPushInterface.EXTRA_TITLE);
        String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setSmallIcon(R.drawable.ic_warning);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(MONITOR_WARNING_NOTIFICATION_ID, mBuilder.build());
    }
}
