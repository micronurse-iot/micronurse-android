package org.micronurse.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.app.Service;
import android.os.Vibrator;

/**
 * Created by lsq on 2016/11/3.
 */
public class AlarmReceiver extends BroadcastReceiver {
    private Vibrator vibrator;
    @Override
    public void onReceive(Context context, Intent intent) {
        AlertDialog.Builder medicineReminder = new AlertDialog.Builder(context);
        medicineReminder.setTitle("闹钟");
        medicineReminder.setMessage("提醒吃药");
        medicineReminder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                vibrator.cancel();
            }
        });
        vibrator = (Vibrator)context.getSystemService(Service.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[]{100,10,10,100},0);
        medicineReminder.show();
    }

}