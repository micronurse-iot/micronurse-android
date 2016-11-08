package org.micronurse.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.micronurse.Application;
import org.micronurse.database.model.MedicationReminder;
import org.micronurse.receiver.MedicationReminderReceiver;

import static com.activeandroid.Cache.getContext;

/**
 * Created by zhou-shengyun on 16-11-8.
 */

public class ScheduleUtil {

    public static void scheduleMedicationRemider(Context context, MedicationReminder reminder){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent reminderIntent = new Intent(Application.ACTION_MEDICATION_REMINDER);
        reminderIntent.addCategory(context.getPackageName());
        reminderIntent.setClass(context, MedicationReminderReceiver.class);
        reminderIntent.putExtra(MedicationReminderReceiver.BUNDLE_KEY_MEDICINE_NAME, reminder.getMedicineName());
        reminderIntent.putExtra(MedicationReminderReceiver.BUNDLE_KEY_MEDECINE_USAGE, reminder.getMedicineUsage());
        reminderIntent.putExtra(MedicationReminderReceiver.BUNDLE_KEY_REMINDER_ID, reminder.getId());

        PendingIntent pi = PendingIntent.getBroadcast(getContext(), reminder.getId().intValue(), reminderIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if(!reminder.isSwitchOn()) {
            alarmManager.cancel(pi);
            return;
        }

        long nextRemindTime = reminder.getNextRemindTime();
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextRemindTime, pi);
    }
}
