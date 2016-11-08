package org.micronurse.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.model.User;
import org.micronurse.util.GlobalInfo;

/**
 * Created by zhou-shengyun on 9/7/16.
 */
public class MonitorWarningReceiver extends BroadcastReceiver {
    public static final int MONITOR_WARNING_NOTIFICATION_ID = 34985723;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(GlobalInfo.user == null)
            return;
        Bundle bundle = intent.getExtras();
        String title = context.getString(R.string.action_monitor_warning);
        String message = bundle.getString(Application.BUNDLE_KEY_MESSAGE);
        String userId = bundle.getString(Application.BUNDLE_KEY_USER_ID);
        if(GlobalInfo.user.getPhoneNumber().equals(userId))
            title += '-' + GlobalInfo.user.getNickname();
        else{
            if(GlobalInfo.guardianshipList != null){
                for(User u : GlobalInfo.guardianshipList){
                    if(u.getPhoneNumber().equals(userId)) {
                        title += '-' + u.getNickname();
                        break;
                    }
                }
            }
        }
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
