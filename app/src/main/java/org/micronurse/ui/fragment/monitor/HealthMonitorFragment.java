package org.micronurse.ui.fragment.monitor;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.FeverThermometerDataListResult;
import org.micronurse.http.model.result.PulseTransducerDataListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.TurgoscopeDataListResult;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.Turgoscope;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.GlobalInfo;


public class HealthMonitorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View viewRoot;
    private TextView txtHealthCondition;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView healthDataList;

    private FeverThermometer feverThermometer;
    private PulseTransducer pulseTransducer;
    private Turgoscope turgoscope;
    private List<Object> sensorDataList = new ArrayList<>();
    private Timer scheduleTask;
    private String updateBodyTemperatureURL;
    private String updatePulseURL;
    private String updateBodyBloodPressureURL;

    public HealthMonitorFragment() {
        // Required empty public constructor
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            updateBodyTemperatureURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_FEVER_THERMOMETER,
                    String.valueOf(1));
            updatePulseURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_PULSE_TRANSDUCER,
                    String.valueOf(1));
            updateBodyBloodPressureURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_TURGOSCOPE,
                    String.valueOf(1));
        }else if(GlobalInfo.Guardian.monitorOlder != null){
            updateBodyTemperatureURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_FEVER_THERMOMETER, String.valueOf(1));
            updatePulseURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_PULSE_TRANSDUCER, String.valueOf(1));
            updateBodyBloodPressureURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_TURGOSCOPE, String.valueOf(1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_health_monitor, container, false);
        swipeLayout = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        swipeLayout.setColorSchemeResources(R.color.colorAccent);
        swipeLayout.setOnRefreshListener(this);

        txtHealthCondition = (TextView) viewRoot.findViewById(R.id.health_condition);
        healthDataList = (RecyclerView) viewRoot.findViewById(R.id.health_data_list);
        healthDataList.setLayoutManager(new LinearLayoutManager(getContext()));
        healthDataList.setNestedScrollingEnabled(false);
        return viewRoot;
    }

    private void startScheduleTask(){
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                GlobalInfo.Guardian.monitorOlder == null){
            scheduleTask = null;
            viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.VISIBLE);
            viewRoot.findViewById(R.id.health_data_area).setVisibility(View.GONE);
            swipeLayout.setEnabled(false);
        }else {
            if(scheduleTask != null)
                return;
            scheduleTask = new Timer();
            swipeLayout.setRefreshing(true);
            scheduleTask.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateData();
                }
            }, 0, 5000);
        }
    }

    @Override
    public void onPause() {
        if(scheduleTask != null) {
            scheduleTask.cancel();
            scheduleTask = null;
        }
        super.onPause();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            if(scheduleTask != null) {
                scheduleTask.cancel();
                scheduleTask = null;
            }
        }else{
            startScheduleTask();
        }
    }

    @Override
    public void onRefresh() {
        updateData();
    }

    private void updateData(){
        new MicronurseAPI<>(getContext(), updateBodyTemperatureURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<FeverThermometerDataListResult>() {
            @Override
            public void onResponse(FeverThermometerDataListResult response) {
                feverThermometer = response.getDataList().get(0);
                swipeLayout.setRefreshing(false);
                updateDataView();
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                swipeLayout.setRefreshing(false);
            }
        }, FeverThermometerDataListResult.class, false, null).startRequest();

        new MicronurseAPI<>(getContext(), updatePulseURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<PulseTransducerDataListResult>() {
            @Override
            public void onResponse(PulseTransducerDataListResult response) {
                pulseTransducer = response.getDataList().get(0);
                swipeLayout.setRefreshing(false);
                updateDataView();
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                swipeLayout.setRefreshing(false);
            }
        }, PulseTransducerDataListResult.class, false, null).startRequest();

        new MicronurseAPI<>(getContext(), updateBodyBloodPressureURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<TurgoscopeDataListResult>() {
            @Override
            public void onResponse(TurgoscopeDataListResult response) {
                turgoscope = response.getDataList().get(0);
                swipeLayout.setRefreshing(false);
                updateDataView();
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                swipeLayout.setRefreshing(false);
            }
        }, TurgoscopeDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateDataView(){
        if(feverThermometer != null || pulseTransducer != null || turgoscope != null){
            viewRoot.findViewById(R.id.health_data_area).setVisibility(View.VISIBLE);
            viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
            CheckUtil.checkHealthSafetyLevel(txtHealthCondition, viewRoot.findViewById(R.id.health_condition_area),
                    feverThermometer, pulseTransducer, turgoscope);
        }
        sensorDataList.clear();
        if(feverThermometer != null)
            sensorDataList.add(feverThermometer);
        if(pulseTransducer != null)
            sensorDataList.add(pulseTransducer);
        if(turgoscope != null)
           sensorDataList.add(turgoscope);
        if(!sensorDataList.isEmpty())
            healthDataList.setAdapter(new MonitorAdapter(getActivity(), sensorDataList));
    }
}

