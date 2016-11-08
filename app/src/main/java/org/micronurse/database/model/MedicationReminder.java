package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by zhou-shengyun on 16-11-7.
 */

@Table(name = "MedicationReminder")
public class MedicationReminder extends Model {
    @Column(name = "UserId", notNull = true)
    private String userId;

    @Column(name = "MedicineName")
    private String medicineName;

    @Column(name = "MedicineUsage")
    private String medicineUsage;

    @Column(name = "RemindTime", notNull = true)
    private Date remindTime;

    @Column(name = "Repeat", length = 7)
    private String repeat;

    @Column(name = "SwitchOn", notNull = true)
    private boolean switchOn;

    @Column(name = "AddTime", notNull = true)
    private Date addTime;

    public MedicationReminder() {
        super();
    }

    public MedicationReminder(String userId, String medicineName, String medicineUsage, Date remindTime, String repeat, boolean switchOn) {
        this.userId = userId;
        this.medicineName = medicineName;
        this.medicineUsage = medicineUsage;
        this.remindTime = remindTime;
        this.repeat = repeat;
        this.switchOn = switchOn;
        this.addTime = new Date();
    }

    public MedicationReminder(String userId, Date remindTime) {
        this.userId = userId;
        this.remindTime = remindTime;
        this.switchOn = false;
        this.addTime = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineUsage() {
        return medicineUsage;
    }

    public void setMedicineUsage(String medicineUsage) {
        this.medicineUsage = medicineUsage;
    }

    public Date getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(Date remindTime) {
        this.remindTime = remindTime;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public boolean isSwitchOn() {
        return switchOn;
    }

    public void setSwitchOn(boolean switchOn) {
        this.switchOn = switchOn;
    }

    public boolean[] getRepeatWeekday(){
        boolean[] result = new boolean[7];
        for(int i = 0; i < 7; i++){
            if(repeat != null && repeat.contains(String.valueOf(i + 1))){
                result[i] = true;
            }else{
                result[i] = false;
            }
        }
        return result;
    }

    public void setRepeatWeekday(boolean[] repeatWeekday){
        int len = (repeatWeekday.length > 7) ? 7 : repeatWeekday.length;
        repeat = "";
        for(int i = 0; i < len; i++){
            if(repeatWeekday[i])
                repeat += String.valueOf(i + 1);
        }
    }

    public long getNextRemindTime(){
        if(!switchOn)
            return -1;

        Calendar currentTime = Calendar.getInstance();
        int currentDate = currentTime.get(Calendar.DAY_OF_MONTH);
        Calendar nextTime = Calendar.getInstance();
        nextTime.set(Calendar.HOUR_OF_DAY, remindTime.getHours());
        nextTime.set(Calendar.MINUTE, remindTime.getMinutes());
        nextTime.set(Calendar.SECOND, 0);
        nextTime.set(Calendar.MILLISECOND, 0);
        boolean[] weekdays = getRepeatWeekday();

        for(int i = currentTime.get(Calendar.DAY_OF_WEEK); i <= 7; i++){
            if(weekdays[i - 1]) {
                nextTime.set(Calendar.DAY_OF_MONTH, currentDate + (i - currentTime.get(Calendar.DAY_OF_WEEK)));
                if(nextTime.after(currentTime))
                    return nextTime.getTimeInMillis();
            }
        }
        int delta = 7 - currentTime.get(Calendar.DAY_OF_WEEK);
        //Next week
        for(int i = 1; i <= currentTime.get(Calendar.DAY_OF_WEEK); i++){
            if(weekdays[i - 1]) {
                nextTime.set(Calendar.DAY_OF_MONTH, currentDate + delta + i);
                if(nextTime.after(currentTime))
                    return nextTime.getTimeInMillis();
            }
        }

        nextTime.set(Calendar.DAY_OF_MONTH, currentDate);
        if(nextTime.after(currentTime))
            return nextTime.getTimeInMillis();
        nextTime.set(Calendar.DAY_OF_MONTH, currentDate + 1);
        return nextTime.getTimeInMillis();
    }
}
