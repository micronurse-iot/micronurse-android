package org.micronurse.adapter;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.micronurse.R;
import org.micronurse.database.model.MedicationReminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by lsq on 2016/10/31.
 */
public class MedicationReminderAdapter extends RecyclerView.Adapter<MedicationReminderAdapter.ItemViewHolder> {
    private Context context;
    private List<MedicationReminder> reminderList = new ArrayList<>();
    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private OnReminderSettingsChangeListener settingsChangeListener;
    private OnReminderDeleteListener deleteListener;

    public MedicationReminderAdapter(Context context, List<MedicationReminder> reminderList) {
        this.context = context;
        this.reminderList = reminderList;
    }

    public void setOnReminderDeleteListener(OnReminderDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setOnReminderSettingsChangeListener(OnReminderSettingsChangeListener listener) {
        this.settingsChangeListener = listener;
    }

    @Override
    public int getItemCount() {
        return reminderList.size();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_medication_data, parent, false));
    }

    private EditText getNewEditText(){
        MaterialEditText editText = new MaterialEditText(context);
        editText.setShowClearButton(true);
        editText.setUnderlineColor(context.getResources().getColor(R.color.grey_500));
        editText.setPrimaryColor(context.getResources().getColor(R.color.colorAccent));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(context.getResources().getDimensionPixelSize(R.dimen.item_padding),
                0, context.getResources().getDimensionPixelSize(R.dimen.item_padding), 0);
        editText.setLayoutParams(lp);
        return editText;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        final MedicationReminder reminder = reminderList.get(position);
        holder.reminderTimeView.setText(timeFormat.format(reminder.getRemindTime()));
        holder.reminderTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar tempTime = Calendar.getInstance();
                tempTime.set(Calendar.HOUR_OF_DAY, reminder.getRemindTime().getHours());
                tempTime.set(Calendar.MINUTE, reminder.getRemindTime().getMinutes());
                TimePickerDialog tdp = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        tempTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        tempTime.set(Calendar.MINUTE, minute);
                        reminder.setRemindTime(tempTime.getTime());
                        holder.reminderTimeView.setText(timeFormat.format(tempTime.getTime()));
                        reminder.save();
                        if(settingsChangeListener != null){
                            settingsChangeListener.onReminderSettingsChange(reminder);
                        }
                    }
                }, tempTime.get(Calendar.HOUR_OF_DAY), tempTime.get(Calendar.MINUTE), true);
                tdp.show();
            }
        });
        holder.reminderSwitch.setChecked(reminder.isSwitchOn());
        holder.reminderSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                reminder.setSwitchOn(isChecked);
                reminder.save();
                if(settingsChangeListener != null){
                    settingsChangeListener.onReminderSettingsChange(reminder);
                }
            }
        });

        holder.medicineNameView.setText(reminder.getMedicineName());
        holder.medicineNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = getNewEditText();
                editText.setText(reminder.getMedicineName());
                new AlertDialog.Builder(context).setTitle(R.string.medicine_name)
                        .setView(editText)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                holder.medicineNameView.setText(editText.getText().toString());
                                reminder.setMedicineName(editText.getText().toString());
                                reminder.save();
                                if(settingsChangeListener != null){
                                    settingsChangeListener.onReminderSettingsChange(reminder);
                                }
                            }
                        })
                        .show();
            }
        });
        holder.medicineUsageView.setText(reminder.getMedicineUsage());
        holder.medicineUsageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText editText = getNewEditText();
                editText.setText(reminder.getMedicineUsage());
                new AlertDialog.Builder(context).setTitle(R.string.medicine_usage)
                        .setView(editText)
                        .setNegativeButton(R.string.action_cancel, null)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                holder.medicineUsageView.setText(editText.getText().toString());
                                reminder.setMedicineUsage(editText.getText().toString());
                                reminder.save();
                                if(settingsChangeListener != null){
                                    settingsChangeListener.onReminderSettingsChange(reminder);
                                }
                            }
                        })
                        .show();
            }
        });

        final boolean[] weekdays = reminder.getRepeatWeekday();
        for(int i = 0; i < weekdays.length; i++){
            holder.btnWeekdays[i].setChecked(weekdays[i]);
            final int pos = i;
            holder.btnWeekdays[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    weekdays[pos] = isChecked;
                    reminder.setRepeatWeekday(weekdays);
                    reminder.save();
                    if(settingsChangeListener != null){
                        settingsChangeListener.onReminderSettingsChange(reminder);
                    }
                }
            });
        }

        holder.btnDeleteReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog ad = new AlertDialog.Builder(context).setMessage(R.string.query_delete_reminder)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(deleteListener != null)
                                    deleteListener.onReminderDelete(holder.getAdapterPosition(), reminder);
                            }
                        }).setNegativeButton(R.string.action_cancel, null).create();
                ad.show();
            }
        });
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private static final String BTN_WEEKDAYS_TAG_PRIFIX = "btn_weekday_";

        private View itemView;
        private EditText medicineNameView;
        private EditText medicineUsageView;
        private ToggleButton[] btnWeekdays;
        private SwitchCompat reminderSwitch;
        private TextView reminderTimeView;
        private ImageButton btnDeleteReminder;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            medicineNameView = (EditText) itemView.findViewById(R.id.medicine_name);
            medicineUsageView = (EditText) itemView.findViewById(R.id.medicine_usage);
            reminderSwitch = (SwitchCompat) itemView.findViewById(R.id.reminder_switch);
            reminderTimeView = (TextView) itemView.findViewById(R.id.reminder_time);
            btnDeleteReminder = (ImageButton) itemView.findViewById(R.id.btn_delete_reminder);
            btnWeekdays = new ToggleButton[7];
            String[] weekdayShortStr = context.getResources().getStringArray(R.array.weekday_short);
            for(int i = 0; i < 7; i++){
                btnWeekdays[i] = (ToggleButton) itemView.findViewWithTag(BTN_WEEKDAYS_TAG_PRIFIX + String.valueOf(i + 1));
                btnWeekdays[i].setTextOn(weekdayShortStr[i]);
                btnWeekdays[i].setTextOff(weekdayShortStr[i]);
            }
        }
    }

    public interface OnReminderSettingsChangeListener{
        void onReminderSettingsChange(MedicationReminder reminder);
    }

    public interface OnReminderDeleteListener{
        void onReminderDelete(int pos, MedicationReminder reminder);
    }
}
