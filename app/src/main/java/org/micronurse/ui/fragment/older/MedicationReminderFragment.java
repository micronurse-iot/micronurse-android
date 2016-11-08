package org.micronurse.ui.fragment.older;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import org.micronurse.Application;
import org.micronurse.adapter.MedicationReminderAdapter;
import org.micronurse.R;

import org.micronurse.database.model.MedicationReminder;
import org.micronurse.receiver.MedicationReminderReceiver;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ScheduleUtil;

import java.util.Calendar;
import java.util.LinkedList;

public class MedicationReminderFragment extends Fragment{
    private View viewRoot;
    private RecyclerView medicineDataListView;
    private FloatingActionButton btnAddRemider;

    private MedicationReminderAdapter medicationReminderAdapter;
    private LinkedList<MedicationReminder> reminderList = new LinkedList<>();
    private Calendar reminderTime;

    public MedicationReminderFragment() {
        // Required empty public constructor
        reminderTime = Calendar.getInstance();
    }

    public static MedicationReminderFragment getInstance(Context context) {
        MedicationReminderFragment fragment = new MedicationReminderFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_older_medication_reminder, container, false);
        medicineDataListView = (RecyclerView) viewRoot.findViewById(R.id.medicine_data_list);
        medicineDataListView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAddRemider = (FloatingActionButton) viewRoot.findViewById(R.id.btn_add_medication_reminder);
        btnAddRemider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminderTime.setTimeInMillis(System.currentTimeMillis());
                TimePickerDialog tpd = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        reminderTime.set(Calendar.MINUTE, minute);
                        MedicationReminder newReminder = new MedicationReminder(GlobalInfo.user.getPhoneNumber(), reminderTime.getTime());
                        newReminder.save();
                        reminderList.addFirst(newReminder);
                        medicationReminderAdapter.notifyItemInserted(0);
                        viewRoot.findViewById(R.id.txt_no_medication_reminder).setVisibility(View.GONE);
                    }
                }, reminderTime.get(Calendar.HOUR_OF_DAY), reminderTime.get(Calendar.MINUTE), true);
                tpd.show();
            }
        });

        reminderList.addAll(DatabaseUtil.findMedicationRemindersByUserId(GlobalInfo.user.getPhoneNumber()));
        if(!reminderList.isEmpty())
            viewRoot.findViewById(R.id.txt_no_medication_reminder).setVisibility(View.GONE);
        medicationReminderAdapter = new MedicationReminderAdapter(getActivity(), reminderList);
        medicationReminderAdapter.setOnReminderDeleteListener(new MedicationReminderAdapter.OnReminderDeleteListener() {
            @Override
            public void onReminderDelete(int pos, MedicationReminder reminder) {
                reminderList.remove(pos);
                medicationReminderAdapter.notifyItemRemoved(pos);
                if(reminderList.isEmpty())
                    viewRoot.findViewById(R.id.txt_no_medication_reminder).setVisibility(View.VISIBLE);
                reminder.setSwitchOn(false);
                ScheduleUtil.scheduleMedicationRemider(getContext(), reminder);
                reminder.delete();
            }
        });
        medicationReminderAdapter.setOnReminderSettingsChangeListener(new MedicationReminderAdapter.OnReminderSettingsChangeListener() {
            @Override
            public void onReminderSettingsChange(MedicationReminder reminder) {
                ScheduleUtil.scheduleMedicationRemider(getContext(), reminder);
            }
        });
        medicineDataListView.setAdapter(medicationReminderAdapter);

        for(MedicationReminder mr : reminderList)
            ScheduleUtil.scheduleMedicationRemider(getContext(), mr);

        return viewRoot;
    }
}