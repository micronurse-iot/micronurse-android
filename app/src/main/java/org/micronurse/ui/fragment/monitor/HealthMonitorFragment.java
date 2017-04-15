package org.micronurse.ui.fragment.monitor;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.FeverThermometerDataListResult;
import org.micronurse.net.model.result.PulseTransducerDataListResult;
import org.micronurse.net.model.result.Result;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.User;
import org.micronurse.ui.listener.OnSensorDataReceivedListener;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.GlobalInfo;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HealthMonitorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnSensorDataReceivedListener {
    private View rootView;
    @BindView(R.id.txt_health_condition)
    TextView txtHealthCondition;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;
    @BindView(R.id.health_data_list)
    RecyclerView healthDataListView;

    private FeverThermometer feverThermometer;
    private PulseTransducer pulseTransducer;
    private List<Sensor> sensorDataList = new ArrayList<>();
    private MonitorAdapter adapter;
    private String updateBodyTemperatureURL;
    private String updatePulseURL;

    public HealthMonitorFragment() {
        // Required empty public constructor
    }

    public static HealthMonitorFragment getInstance(Context context){
        HealthMonitorFragment fragment = new HealthMonitorFragment();
        return fragment;
    }

    private void updateURL(){
        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_OLDER:
                updateBodyTemperatureURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        Sensor.SENSOR_TYPE_FEVER_THERMOMETER, String.valueOf(1));
                updatePulseURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        Sensor.SENSOR_TYPE_PULSE_TRANSDUCER, String.valueOf(1));
                break;
            case User.ACCOUNT_TYPE_GUARDIAN:
                if(GlobalInfo.Guardian.monitorOlder == null){
                    refresh.setEnabled(false);
                    break;
                }
                updateBodyTemperatureURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                        Sensor.SENSOR_TYPE_FEVER_THERMOMETER, String.valueOf(1));
                updatePulseURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                        Sensor.SENSOR_TYPE_PULSE_TRANSDUCER, String.valueOf(1));
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
        rootView = inflater.inflate(R.layout.fragment_health_monitor, container, false);
        ButterKnife.bind(this, rootView);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(this);

        healthDataListView.setLayoutManager(new LinearLayoutManager(getContext()));
        healthDataListView.setNestedScrollingEnabled(false);
        adapter = new MonitorAdapter(getActivity(), sensorDataList);
        healthDataListView.setAdapter(adapter);

        updateURL();
        if(refresh.isEnabled()){
            refresh.setRefreshing(true);
            updateData();
        }
        return rootView;
    }

    @Override
    public void onRefresh() {
        updateData();
    }

    private void updateData(){
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), updateBodyTemperatureURL, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<FeverThermometerDataListResult>(FeverThermometerDataListResult.class) {
                    @Override
                    public void onResponse() {
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onDataResponse(FeverThermometerDataListResult data) {
                        updateBodyTemperature(data.getDataList().get(0));
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        return errorInfo.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND;
                    }
                }));

        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), updatePulseURL, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<PulseTransducerDataListResult>(PulseTransducerDataListResult.class) {
                    @Override
                    public void onResponse() {
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onDataResponse(PulseTransducerDataListResult data) {
                        updatePulse(data.getDataList().get(0));
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        return errorInfo.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND;
                    }
                }));
    }

    private synchronized void updateBodyTemperature(FeverThermometer feverThermometer){
        if(this.feverThermometer != null) {
            if (feverThermometer.getTimestamp().getTime() <= this.feverThermometer.getTimestamp().getTime())
                return;
        }
        this.feverThermometer = feverThermometer;
        updateDataView();
    }

    private synchronized void updatePulse(PulseTransducer pulseTransducer){
        if(this.pulseTransducer != null) {
            if (pulseTransducer.getTimestamp().getTime() <= this.pulseTransducer.getTimestamp().getTime())
                return;
        }
        this.pulseTransducer = pulseTransducer;
        updateDataView();
    }

    @SuppressLint("SetTextI18n")
    private void updateDataView(){
        if(feverThermometer != null || pulseTransducer != null){
            rootView.findViewById(R.id.health_data_area).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
            CheckUtil.checkHealthSafetyLevel(txtHealthCondition, rootView.findViewById(R.id.health_condition_area),
                    feverThermometer, pulseTransducer);
        }
        sensorDataList.clear();
        if(feverThermometer != null)
            sensorDataList.add(feverThermometer);
        if(pulseTransducer != null)
            sensorDataList.add(pulseTransducer);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSensorDataReceived(Sensor sensor) {
        if (rootView == null)
            return;
        if (sensor instanceof FeverThermometer)
            updateBodyTemperature((FeverThermometer) sensor);
        else if (sensor instanceof PulseTransducer)
            updatePulse((PulseTransducer) sensor);
    }
}

