package org.micronurse.ui.fragment.monitor;

import android.content.Context;
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

import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.HumidometerDataListResult;
import org.micronurse.net.model.result.Result;
import org.micronurse.net.model.result.SmokeTransducerDataListResult;
import org.micronurse.net.model.result.ThermometerDataListResult;
import org.micronurse.model.Humidometer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.User;
import org.micronurse.ui.listener.OnSensorDataReceivedListener;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.GlobalInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FamilyMonitorFragment extends Fragment implements OnSensorDataReceivedListener{
    private View rootView;
    @BindView(R.id.txt_safe_level)
    TextView txtSafeLevel;
    @BindView(R.id.temperature_list)
    RecyclerView temperatureListView;
    @BindView(R.id.humidity_list)
    RecyclerView humidityListView;
    @BindView(R.id.smoke_list)
    RecyclerView smokeListView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;

    private List<Thermometer> thermometerList = new ArrayList<>();
    private MonitorAdapter thermometerAdapter;
    private List<Humidometer> humidometerList = new ArrayList<>();
    private MonitorAdapter humidometerAdapter;
    private List<SmokeTransducer> smokeTransducerList = new ArrayList<>();
    private MonitorAdapter smokeAdapter;

    private String updateTemperatureURL;
    private String updateHumidityURL;
    private String updateSmokeURL;

    public FamilyMonitorFragment() {
        // Required empty public constructor
    }

    public static FamilyMonitorFragment getInstance(Context context){
        FamilyMonitorFragment fragment = new FamilyMonitorFragment();
        return fragment;
    }

    private void updateURL(){
        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_OLDER:
                updateTemperatureURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        Sensor.SENSOR_TYPE_THERMOMETER, String.valueOf(1));
                updateHumidityURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        Sensor.SENSOR_TYPE_HUMIDOMETER, String.valueOf(1));
                updateSmokeURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER, String.valueOf(1));
                break;
            case User.ACCOUNT_TYPE_GUARDIAN:
                if(GlobalInfo.Guardian.monitorOlder == null){
                    refresh.setEnabled(false);
                    break;
                }
                updateTemperatureURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                        Sensor.SENSOR_TYPE_THERMOMETER, String.valueOf(1));
                updateHumidityURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                        Sensor.SENSOR_TYPE_HUMIDOMETER, String.valueOf(1));
                updateSmokeURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                        Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER, String.valueOf(1));
                break;
            default:
                refresh.setEnabled(false);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_family_monitor, container, false);
        ButterKnife.bind(this, rootView);

        refresh.setColorSchemeResources(R.color.colorAccent);
        temperatureListView.setLayoutManager(new LinearLayoutManager(getContext()));
        temperatureListView.setNestedScrollingEnabled(false);
        thermometerAdapter = new MonitorAdapter(getActivity(), thermometerList);
        temperatureListView.setAdapter(thermometerAdapter);

        humidityListView.setLayoutManager(new LinearLayoutManager(getContext()));
        humidityListView.setNestedScrollingEnabled(false);
        humidometerAdapter = new MonitorAdapter(getActivity(), humidometerList);
        humidityListView.setAdapter(humidometerAdapter);

        smokeListView.setLayoutManager(new LinearLayoutManager(getContext()));
        smokeListView.setNestedScrollingEnabled(false);
        smokeAdapter = new MonitorAdapter(getActivity(), smokeTransducerList);
        smokeListView.setAdapter(smokeAdapter);

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
        return rootView;
    }

    private void updateSafeLevel(){
        CheckUtil.checkFamilySafetyLevel(txtSafeLevel, rootView.findViewById(R.id.safe_level_area),
                thermometerList, humidometerList, smokeTransducerList);
        thermometerAdapter.notifyDataSetChanged();
        humidometerAdapter.notifyDataSetChanged();
        smokeAdapter.notifyDataSetChanged();
    }

    private void updateTemperature(){
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), updateTemperatureURL, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<ThermometerDataListResult>(ThermometerDataListResult.class) {
                    @Override
                    public void onResponse() {
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onDataResponse(ThermometerDataListResult data) {
                        for(Thermometer t : data.getDataList())
                            updateTemperature(t);
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        return errorInfo.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND;
                    }
                }));
    }

    private synchronized void updateTemperature(Thermometer thermometer){
        ButterKnife.findById(rootView, R.id.safe_level_area).setVisibility(View.VISIBLE);
        ButterKnife.findById(rootView, R.id.txt_no_data).setVisibility(View.GONE);
        ButterKnife.findById(rootView, R.id.temperature_area).setVisibility(View.VISIBLE);
        for(Thermometer t : thermometerList){
            if(t.getName().equals(thermometer.getName())){
                if(thermometer.getTimestamp().getTime() <= t.getTimestamp().getTime())
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
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), updateHumidityURL, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<HumidometerDataListResult>(HumidometerDataListResult.class) {
                    @Override
                    public void onResponse() {
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onDataResponse(HumidometerDataListResult data) {
                        for(Humidometer h : data.getDataList())
                            updateHumidity(h);
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        return errorInfo.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND;
                    }
                }));
    }

    private synchronized void updateHumidity(Humidometer humidometer){
        rootView.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        rootView.findViewById(R.id.humidity_area).setVisibility(View.VISIBLE);
        for(Humidometer h : humidometerList){
            if(h.getName().equals(humidometer.getName())){
                if(humidometer.getTimestamp().getTime() <= h.getTimestamp().getTime())
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
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), updateSmokeURL, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<SmokeTransducerDataListResult>(SmokeTransducerDataListResult.class) {
                    @Override
                    public void onResponse() {
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onDataResponse(SmokeTransducerDataListResult data) {
                        refresh.setRefreshing(false);
                        for(SmokeTransducer st : data.getDataList())
                            updateSmoke(st);
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        return errorInfo.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND;
                    }
                }));
    }

    private synchronized void updateSmoke(SmokeTransducer smokeTransducer){
        rootView.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        rootView.findViewById(R.id.smoke_area).setVisibility(View.VISIBLE);
        for(SmokeTransducer st : smokeTransducerList){
            if(st.getName().equals(smokeTransducer.getName())){
                if(smokeTransducer.getTimestamp().getTime() <= st.getTimestamp().getTime())
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
    public void onSensorDataReceived(Sensor sensor) {
        if(rootView == null)
            return;
        if (sensor instanceof Thermometer)
            updateTemperature((Thermometer) sensor);
        else if (sensor instanceof Humidometer)
            updateHumidity((Humidometer) sensor);
        else if (sensor instanceof SmokeTransducer)
            updateSmoke((SmokeTransducer) sensor);
    }

}
