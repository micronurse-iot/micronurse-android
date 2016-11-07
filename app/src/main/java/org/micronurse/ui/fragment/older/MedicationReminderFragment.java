package org.micronurse.ui.fragment.older;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.micronurse.adapter.MedicineListAdapter;
import org.micronurse.R;

import org.micronurse.receiver.AlarmReceiver;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MedicationReminderFragment extends Fragment{
    private static final int INTERVAL = 24 * 60 * 60 * 1000;
    private View viewRoot;
    private RecyclerView medicineDataListView;
    private MedicineListAdapter medicineListsAdapter;
    private List<Integer> medicineList = new ArrayList<>();

    public MedicationReminderFragment() {
        // Required empty public constructor
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
        initData();
        medicineDataListView = (RecyclerView) viewRoot.findViewById(R.id.medicine_data_list);
        medicineDataListView.setLayoutManager(new LinearLayoutManager(getContext()));
        medicineListsAdapter = new MedicineListAdapter(getActivity(), medicineList);
        medicineListsAdapter.setOnItemNameClickListener(new MedicineListAdapter.OnItemNameClickListener() {
            @Override
            public void onNameTextViewClick() {
                showMedicineNameDialog();
            }
        });
        medicineListsAdapter.setOnItemUsageClickListener(new MedicineListAdapter.OnItemUsageClickListener() {
            @Override
            public void onUsageTextViewClick() {
                showMedicineUsageDialog();
            }
        });

        medicineListsAdapter.setOnSetClockClickListener(new MedicineListAdapter.OnSetClockClickListener() {
            @Override
            public void onSetClockClick(int id) {
                showClockDialog(id);
            }
        });

        medicineListsAdapter.setOnCancelClockClickListener(new MedicineListAdapter.OnCancelClockClickListener() {
            @Override
            public void onCancelClockClick(int id) {
                cancelClock(id);
            }
        });


        medicineDataListView.setAdapter(medicineListsAdapter);
        return viewRoot;
    }


    protected void initData() {
        for (int i = 0; i < 5; i++) {
            medicineList.add(i);
        }
    }


    protected void showMedicineNameDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.item_medicine_name, null);
        final EditText edtMedicineName = (EditText) view.findViewById(R.id.item_medicine_name);
        final View viewMedicineData = inflater.inflate(R.layout.item_medication_data, null);
        final TextView txtMedicineName = (TextView) viewMedicineData.findViewById(R.id.medicine_name);
        AlertDialog.Builder medicineName = new AlertDialog.Builder(getActivity());
        medicineName.setView(view);
        medicineName.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                String medicine_name = edtMedicineName.getText().toString();
                txtMedicineName.setText(medicine_name);
            }
        });

        medicineName.setNegativeButton("取消", null);
        medicineName.show();
    }


    protected void showMedicineUsageDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.item_medicine_usage, null);
        final EditText edtMedicineUsage = (EditText) view.findViewById(R.id.item_medicine_usage);
        final View viewMedicineData = inflater.inflate(R.layout.item_medication_data, null);
        final TextView txtMedicineUsage = (TextView) viewMedicineData.findViewById(R.id.medicine_usage);
        AlertDialog.Builder medicineUsage = new AlertDialog.Builder(getActivity());
        medicineUsage.setView(view);
        medicineUsage.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int i) {
                String medicine_name = edtMedicineUsage.getText().toString();
                if (TextUtils.isEmpty(medicine_name))
                    txtMedicineUsage.setText(" ");
                else
                    txtMedicineUsage.setText(medicine_name);
            }
        });

        medicineUsage.setNegativeButton("取消", null);
        medicineUsage.show();
    }

    protected void showClockDialog(final int id) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                //设置日历的时间，主要是让日历的年月日和当前同步
                calendar.setTimeInMillis(System.currentTimeMillis());
                //设置日历的小时和分钟
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                //将秒和毫秒设置为0
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                //建立Intent和PendingIntent来调用闹钟管理器
                Intent intent = new Intent(getActivity(), AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(),id, intent, 0);
                //获取闹钟管理器
                AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Service.ALARM_SERVICE);
                //设置闹钟
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL, pendingIntent);
                Toast.makeText(getActivity(), "设置闹钟的时间为：" + String.valueOf(hour) + ":" + String.valueOf(minute), Toast.LENGTH_SHORT).show();
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
    }


    protected void cancelClock(int id){
        Intent intent = new Intent(getActivity(),AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), id, intent, 0);
        //获取闹钟管理器
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Service.ALARM_SERVICE);
        if(pendingIntent != null)
            alarmManager.cancel(pendingIntent);
        Toast.makeText(getActivity(), "闹钟已经取消!", Toast.LENGTH_SHORT).show();
    }
}