package org.micronurse.ui.activity.older.main.monitor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.micronurse.R;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Turgoscope;
import org.micronurse.util.DateTimeUtil;


public class HealthMonitorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private SwipeRefreshLayout swipeLayout;
    private View feverItem;
    private TextView feverUpdateTime;
    private TextView feverData;
    private FeverThermometer feverThermometer;
    private View pulseItem;
    private TextView pulseUpdateTime;
    private TextView pulseData;
    private PulseTransducer pulseTransducer;
    private View bloodPressureItem;
    private TextView bloodPressureUpdateTime;
    private TextView bloodPressureData;
    private Turgoscope turgoscope;

    public HealthMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.fragment_older_health_monitor, container, false);
        swipeLayout = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        swipeLayout.setColorSchemeResources(R.color.colorAccent);
        swipeLayout.setOnRefreshListener(this);

        feverItem = viewRoot.findViewById(R.id.fever_item);
        ((TextView)feverItem.findViewById(R.id.data_name)).setText(R.string.fever);
        feverUpdateTime = (TextView) feverItem.findViewById(R.id.data_update_time);
        feverData = (TextView) feverItem.findViewById(R.id.data);

        pulseItem = viewRoot.findViewById(R.id.pulse_item);
        ((TextView)pulseItem.findViewById(R.id.data_name)).setText(R.string.pulse);
        pulseUpdateTime = (TextView) pulseItem.findViewById(R.id.data_update_time);
        pulseData = (TextView) pulseItem.findViewById(R.id.data);

        bloodPressureItem = viewRoot.findViewById(R.id.blood_pressure_item);
        ((TextView)bloodPressureItem.findViewById(R.id.data_name)).setText(R.string.blood_pressure);
        bloodPressureUpdateTime = (TextView) bloodPressureItem.findViewById(R.id.data_update_time);
        bloodPressureData = (TextView) bloodPressureItem.findViewById(R.id.data);

        //Test Data
        feverThermometer = new FeverThermometer(System.currentTimeMillis(), (float)37.5);
        pulseTransducer = new PulseTransducer(System.currentTimeMillis(), 74);
        turgoscope = new Turgoscope(System.currentTimeMillis(), 76, 123);
        updateData();

        return viewRoot;
    }


    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(false);
    }

    @SuppressLint("SetTextI18n")
    private void updateData(){
        if(feverThermometer == null)
            feverItem.setVisibility(View.GONE);
        else{
            feverItem.setVisibility(View.VISIBLE);
            feverUpdateTime.setText(DateTimeUtil.convertTimestamp(feverThermometer.getTimestamp()));
            feverData.setText(String.valueOf(feverThermometer.getTemperature()) + "°C");
        }
        if(pulseTransducer == null)
            pulseItem.setVisibility(View.GONE);
        else{
            pulseItem.setVisibility(View.VISIBLE);
            pulseUpdateTime.setText(DateTimeUtil.convertTimestamp(pulseTransducer.getTimestamp()));
            pulseData.setText(String.valueOf(pulseTransducer.getPulse()) + "bpm");
        }if(turgoscope == null){
            bloodPressureItem.setVisibility(View.GONE);
        }else{
            bloodPressureItem.setVisibility(View.VISIBLE);
            bloodPressureUpdateTime.setText(DateTimeUtil.convertTimestamp(turgoscope.getTimestamp()));
            bloodPressureData.setText(String.valueOf(turgoscope.getLowBloodPressure()) + '/' +
                                      String.valueOf(turgoscope.getHighBloodPressure()) + "Pa");
        }
    }
}

