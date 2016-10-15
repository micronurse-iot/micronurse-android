package org.micronurse.ui.fragment.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.google.gson.JsonSyntaxException;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.HumidometerDataListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.SmokeTransducerDataListResult;
import org.micronurse.http.model.result.ThermometerDataListResult;
import org.micronurse.model.Humidometer;
import org.micronurse.model.RawSensorData;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FamilyMonitorFragment extends Fragment {
    private View viewRoot;
    private TextView txtSafeLevel;
    private RecyclerView temperatureList;
    private RecyclerView humidityList;
    private RecyclerView smokeList;
    private SwipeRefreshLayout refresh;

    private List<Thermometer> thermometerList = new ArrayList<>();
    private List<Humidometer> humidometerList = new ArrayList<>();
    private List<SmokeTransducer> smokeTransducerList = new ArrayList<>();

    private String updateTemperatureURL;
    private String updateHumidityURL;
    private String updateSmokeURL;

    private SensorDataReceiver receiver;

    public FamilyMonitorFragment() {
        // Required empty public constructor
        receiver = new SensorDataReceiver();
    }

    public static FamilyMonitorFragment getInstance(Context context){
        FamilyMonitorFragment fragment = new FamilyMonitorFragment();
        IntentFilter intentFilter = new IntentFilter(Application.ACTION_SENSOR_DATA_REPORT);
        intentFilter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.receiver, intentFilter);
        return fragment;
    }

    private void updateURL(){
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            updateTemperatureURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA,
                    Sensor.SENSOR_TYPE_THERMOMETER, String.valueOf(1));
            updateHumidityURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA,
                    Sensor.SENSOR_TYPE_HUMIDOMETER, String.valueOf(1));
            updateSmokeURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA,
                    Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER, String.valueOf(1));
        }
        else if(GlobalInfo.Guardian.monitorOlder != null) {
            updateTemperatureURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_THERMOMETER, String.valueOf(1));
            updateHumidityURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_HUMIDOMETER, String.valueOf(1));
            updateSmokeURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER, String.valueOf(1));
        }else{
            refresh.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_family_monitor, container, false);
        txtSafeLevel = (TextView)viewRoot.findViewById(R.id.safe_level);
        refresh = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        temperatureList = (RecyclerView) viewRoot.findViewById(R.id.temperature_list);
        temperatureList.setLayoutManager(new LinearLayoutManager(getContext()));
        temperatureList.setNestedScrollingEnabled(false);
        humidityList = (RecyclerView) viewRoot.findViewById(R.id.humidity_list);
        humidityList.setLayoutManager(new LinearLayoutManager(getContext()));
        humidityList.setNestedScrollingEnabled(false);
        smokeList = (RecyclerView) viewRoot.findViewById(R.id.smoke_list);
        smokeList.setLayoutManager(new LinearLayoutManager(getContext()));
        smokeList.setNestedScrollingEnabled(false);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //TODO refresh
            @Override
            public void onRefresh() {
                updateTemperature();
                updateHumidity();
                updateSmoke();
            }
        });
        updateURL();

        if(refresh.isEnabled()) {
            refresh.setRefreshing(true);
            updateTemperature();
            updateHumidity();
            updateSmoke();
        }
        return viewRoot;
    }

    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    private void updateSafeLevel(){
        CheckUtil.checkFamilySafetyLevel(txtSafeLevel, viewRoot.findViewById(R.id.safe_level_area),
                thermometerList, humidometerList, smokeTransducerList);
        smokeList.setAdapter(new MonitorAdapter(getActivity(), smokeTransducerList));
        temperatureList.setAdapter(new MonitorAdapter(getActivity(), thermometerList));
        humidityList.setAdapter(new MonitorAdapter(getActivity(), humidometerList));
    }

    private void updateTemperature(){
        new MicronurseAPI<>(getActivity(), updateTemperatureURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<ThermometerDataListResult>() {
            @Override
            public void onResponse(ThermometerDataListResult response) {
                for(Thermometer t : response.getDataList())
                    updateTemperature(t);
                refresh.setRefreshing(false);
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                refresh.setRefreshing(false);
            }
        }, ThermometerDataListResult.class, false, null).startRequest();
    }

    private synchronized void updateTemperature(Thermometer thermometer){
        viewRoot.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
        viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        viewRoot.findViewById(R.id.temperature_area).setVisibility(View.VISIBLE);
        for(Thermometer t : thermometerList){
            if(t.getName().equals(thermometer.getName())){
                if(thermometer.getTimestamp() <= t.getTimestamp())
                    return;
                t.setTemperature(thermometer.getTemperature());
                t.setTimestamp(thermometer.getTimestamp());
                updateSafeLevel();
                return;
            }
        }
        thermometerList.add(thermometer);
        updateSafeLevel();
    }

    private void updateHumidity(){
        new MicronurseAPI<>(getActivity(), updateHumidityURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<HumidometerDataListResult>() {
                    @Override
                    public void onResponse(HumidometerDataListResult response) {
                        for(Humidometer h : response.getDataList())
                            updateHumidity(h);
                        refresh.setRefreshing(false);
                    }
                }, new APIErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err, Result result) {
                        refresh.setRefreshing(false);
                    }
                }, HumidometerDataListResult.class, false, null).startRequest();
    }

    private synchronized void updateHumidity(Humidometer humidometer){
        viewRoot.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
        viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        viewRoot.findViewById(R.id.humidity_area).setVisibility(View.VISIBLE);
        for(Humidometer h : humidometerList){
            if(h.getName().equals(humidometer.getName())){
                if(humidometer.getTimestamp() <= h.getTimestamp())
                    return;
                h.setHumidity(humidometer.getHumidity());
                h.setTimestamp(humidometer.getTimestamp());
                updateSafeLevel();
                return;
            }
        }
        humidometerList.add(humidometer);
        updateSafeLevel();
    }

    private void updateSmoke(){
        new MicronurseAPI<>(getActivity(), updateSmokeURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<SmokeTransducerDataListResult>() {
                    @Override
                    public void onResponse(SmokeTransducerDataListResult response) {
                        for(SmokeTransducer st : response.getDataList())
                            updateSmoke(st);
                        refresh.setRefreshing(false);
                    }
                }, new APIErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err, Result result) {
                        refresh.setRefreshing(false);
                    }
                }, SmokeTransducerDataListResult.class, false, null).startRequest();
    }

    private synchronized void updateSmoke(SmokeTransducer smokeTransducer){
        viewRoot.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
        viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        viewRoot.findViewById(R.id.smoke_area).setVisibility(View.VISIBLE);
        for(SmokeTransducer st : smokeTransducerList){
            if(st.getName().equals(smokeTransducer.getName())){
                if(smokeTransducer.getTimestamp() <= st.getTimestamp())
                    return;
                st.setSmoke(smokeTransducer.getSmoke());
                st.setTimestamp(smokeTransducer.getTimestamp());
                updateSafeLevel();
                return;
            }
        }
        smokeTransducerList.add(smokeTransducer);
        updateSafeLevel();
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private class SensorDataReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(viewRoot == null)
                return;
            String userId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER &&
                    !GlobalInfo.user.getPhoneNumber().equals(userId))
                return;
            else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                    (GlobalInfo.Guardian.monitorOlder == null || !GlobalInfo.Guardian.monitorOlder.getPhoneNumber().equals(userId)))
                return;
            try {
                RawSensorData rawSensorData = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE), RawSensorData.class);
                if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_THERMOMETER))
                    updateTemperature(new Thermometer(rawSensorData.getTimestamp(), rawSensorData.getName(), Float.valueOf(rawSensorData.getValue())));
                else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_HUMIDOMETER))
                    updateHumidity(new Humidometer(rawSensorData.getTimestamp(), rawSensorData.getName(), Float.valueOf(rawSensorData.getValue())));
                else if (rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER))
                    updateSmoke(new SmokeTransducer(rawSensorData.getTimestamp(), rawSensorData.getName(), Integer.valueOf(rawSensorData.getValue())));
            }catch (NumberFormatException | JsonSyntaxException e){
                e.printStackTrace();
            }
        }
    }
}
