package org.micronurse.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.app.Service;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.database.model.MedicationReminder;
import org.micronurse.model.User;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ScheduleUtil;

/**
 * Created by lsq on 2016/11/3.
 */
public class MedicationReminderReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1239832;

    public static final String BUNDLE_KEY_MEDICINE_NAME = "MedicineName";
    public static final String BUNDLE_KEY_MEDECINE_USAGE = "MedicineUsage";
    public static final String BUNDLE_KEY_REMINDER_ID = "ReminderId";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = context.getString(R.string.action_medication_reminder);
        String message = context.getString(R.string.tip_medication_remind);
        String medicineName = intent.getStringExtra(BUNDLE_KEY_MEDICINE_NAME);
        if(medicineName != null && !medicineName.isEmpty()){
            message = medicineName + '-';
        }
        String medicineUsage = intent.getStringExtra(BUNDLE_KEY_MEDECINE_USAGE);
        if(medicineUsage != null && !medicineUsage.isEmpty())
            message += medicineUsage;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setSmallIcon(R.drawable.ic_alarm);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        long remindId = intent.getLongExtra(BUNDLE_KEY_REMINDER_ID, -1);
        if(remindId < 0)
            return;
        MedicationReminder mr = DatabaseUtil.findMedicationReminderById(remindId);
        if(mr == null) {
            return;
        }
        for(boolean b : mr.getRepeatWeekday()){
            if(b) {
                ScheduleUtil.scheduleMedicationRemider(context, mr);
                return;
            }
        }
        mr.setSwitchOn(false);
        mr.save();
    }
}