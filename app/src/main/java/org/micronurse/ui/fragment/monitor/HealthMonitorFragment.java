package org.micronurse.ui.fragment.monitor;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import org.micronurse.Application;
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
import org.micronurse.model.RawSensorData;
import org.micronurse.model.Sensor;
import org.micronurse.model.Turgoscope;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;


public class HealthMonitorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View viewRoot;
    private TextView txtHealthCondition;
    private SwipeRefreshLayout swipeLayout;
    private RecyclerView healthDataList;

    private FeverThermometer feverThermometer;
    private PulseTransducer pulseTransducer;
    private Turgoscope turgoscope;
    private List<Object> sensorDataList = new ArrayList<>();
    private String updateBodyTemperatureURL;
    private String updatePulseURL;
    private String updateBloodPressureURL;

    private SensorDataReceiver receiver;

    public HealthMonitorFragment() {
        // Required empty public constructor
    }

    private void updateURL(){
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            updateBodyTemperatureURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_FEVER_THERMOMETER,
                    String.valueOf(1));
            updatePulseURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_PULSE_TRANSDUCER,
                    String.valueOf(1));
            updateBloodPressureURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_TURGOSCOPE,
                    String.valueOf(1));
        }else if(GlobalInfo.Guardian.monitorOlder != null){
            updateBodyTemperatureURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_FEVER_THERMOMETER, String.valueOf(1));
            updatePulseURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_PULSE_TRANSDUCER, String.valueOf(1));
            updateBloodPressureURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_TURGOSCOPE, String.valueOf(1));
        }else{
            swipeLayout.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_health_monitor, container, false);
        swipeLayout = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        swipeLayout.setColorSchemeResources(R.color.colorAccent);
        swipeLayout.setOnRefreshListener(this);

        txtHealthCondition = (TextView) viewRoot.findViewById(R.id.health_condition);
        healthDataList = (RecyclerView) viewRoot.findViewById(R.id.health_data_list);
        healthDataList.setLayoutManager(new LinearLayoutManager(getContext()));
        healthDataList.setNestedScrollingEnabled(false);

        updateURL();
        if(swipeLayout.isEnabled()){
            swipeLayout.setRefreshing(true);
            updateData();
        }
        receiver = new SensorDataReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_SENSOR_DATA_REPORT);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(receiver, filter);
        return viewRoot;
    }

    @Override
    public void onRefresh() {
        updateData();
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void updateData(){
        new MicronurseAPI<>(getContext(), updateBodyTemperatureURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<FeverThermometerDataListResult>() {
            @Override
            public void onResponse(FeverThermometerDataListResult response) {
                updateBodyTemperature(response.getDataList().get(0));
                swipeLayout.setRefreshing(false);
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
                updatePulse(response.getDataList().get(0));
                swipeLayout.setRefreshing(false);
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                swipeLayout.setRefreshing(false);
            }
        }, PulseTransducerDataListResult.class, false, null).startRequest();

        new MicronurseAPI<>(getContext(), updateBloodPressureURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<TurgoscopeDataListResult>() {
            @Override
            public void onResponse(TurgoscopeDataListResult response) {
                updateBloodPressure(response.getDataList().get(0));
                swipeLayout.setRefreshing(false);
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                swipeLayout.setRefreshing(false);
            }
        }, TurgoscopeDataListResult.class, false, null).startRequest();
    }

    private synchronized void updateBodyTemperature(FeverThermometer feverThermometer){
        if(this.feverThermometer != null) {
            if (feverThermometer.getTimestamp() <= this.feverThermometer.getTimestamp())
                return;
        }
        this.feverThermometer = feverThermometer;
        updateDataView();
    }

    private synchronized void updatePulse(PulseTransducer pulseTransducer){
        if(this.pulseTransducer != null) {
            if (pulseTransducer.getTimestamp() <= this.pulseTransducer.getTimestamp())
                return;
        }
        this.pulseTransducer = pulseTransducer;
        updateDataView();
    }

    private synchronized void updateBloodPressure(Turgoscope turgoscope){
        if(this.turgoscope != null) {
            if (turgoscope.getTimestamp() <= this.turgoscope.getTimestamp())
                return;
        }
        this.turgoscope = turgoscope;
        updateDataView();
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

    private class SensorDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER &&
                    !GlobalInfo.user.getPhoneNumber().equals(userId))
                return;
            else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                    (GlobalInfo.Guardian.monitorOlder == null || !GlobalInfo.Guardian.monitorOlder.getPhoneNumber().equals(userId)))
                return;
            try {
                RawSensorData rawSensorData = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE), RawSensorData.class);
                if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_FEVER_THERMOMETER))
                    updateBodyTemperature(new FeverThermometer(rawSensorData.getTimestamp(), Float.valueOf(rawSensorData.getValue())));
                else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_PULSE_TRANSDUCER))
                    updatePulse(new PulseTransducer(rawSensorData.getTimestamp(), Integer.valueOf(rawSensorData.getValue())));
                else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_TURGOSCOPE)){
                    String[] splitStr = rawSensorData.getValue().split("/", 2);
                    if(splitStr.length != 2)
                        return;
                    updateBloodPressure(new Turgoscope(rawSensorData.getTimestamp(), Integer.valueOf(splitStr[0]),
                            Integer.valueOf(splitStr[1])));
                }
            }catch (NumberFormatException | JsonSyntaxException e){
                e.printStackTrace();
            }
        }
    }
}

