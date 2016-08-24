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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.micronurse.R;
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
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;


public class HealthMonitorFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private View viewRoot;
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
    private Timer scheduleTask;

    public HealthMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewRoot = inflater.inflate(R.layout.fragment_older_health_monitor, container, false);
        swipeLayout = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        swipeLayout.setColorSchemeResources(R.color.colorAccent);
        swipeLayout.setOnRefreshListener(this);

        feverItem = viewRoot.findViewById(R.id.fever_item);
        feverItem.setVisibility(View.GONE);
        ((TextView)feverItem.findViewById(R.id.data_name)).setText(R.string.fever);
        feverUpdateTime = (TextView) feverItem.findViewById(R.id.data_update_time);
        feverData = (TextView) feverItem.findViewById(R.id.data);

        pulseItem = viewRoot.findViewById(R.id.pulse_item);
        pulseItem.setVisibility(View.GONE);
        ((TextView)pulseItem.findViewById(R.id.data_name)).setText(R.string.pulse);
        pulseUpdateTime = (TextView) pulseItem.findViewById(R.id.data_update_time);
        pulseData = (TextView) pulseItem.findViewById(R.id.data);

        bloodPressureItem = viewRoot.findViewById(R.id.blood_pressure_item);
        bloodPressureItem.setVisibility(View.GONE);
        ((TextView)bloodPressureItem.findViewById(R.id.data_name)).setText(R.string.blood_pressure);
        bloodPressureUpdateTime = (TextView) bloodPressureItem.findViewById(R.id.data_update_time);
        bloodPressureData = (TextView) bloodPressureItem.findViewById(R.id.data);

        return viewRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
        scheduleTask = new Timer();
        swipeLayout.setRefreshing(true);
        scheduleTask.schedule(new TimerTask() {
            @Override
            public void run() {
                updateData();
            }
        }, 0, 5000);
    }

    @Override
    public void onPause() {
        scheduleTask.cancel();
        super.onPause();
    }

    @Override
    public void onRefresh() {
        updateData();
    }

    private void updateData(){
        new MicronurseAPI<>(getContext(), MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_FEVER_THERMOMETER,
                String.valueOf(1)), Request.Method.GET, null, GlobalInfo.token, new Response.Listener<FeverThermometerDataListResult>() {
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

        new MicronurseAPI<>(getContext(), MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_PULSE_TRANSDUCER,
                String.valueOf(1)), Request.Method.GET, null, GlobalInfo.token, new Response.Listener<PulseTransducerDataListResult>() {
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

        new MicronurseAPI<>(getContext(), MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_TURGOSCOPE,
                String.valueOf(1)), Request.Method.GET, null, GlobalInfo.token, new Response.Listener<TurgoscopeDataListResult>() {
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
            viewRoot.findViewById(R.id.health_condition_area).setVisibility(View.VISIBLE);
            viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        }
        if(feverThermometer != null) {
            feverItem.setVisibility(View.VISIBLE);
            feverUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), feverThermometer.getTimestamp()));
            feverData.setText(String.valueOf(feverThermometer.getTemperature()) + "°C");
        }
        if(pulseTransducer != null) {
            pulseItem.setVisibility(View.VISIBLE);
            pulseUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), pulseTransducer.getTimestamp()));
            pulseData.setText(String.valueOf(pulseTransducer.getPulse()) + "bpm");
        }if(turgoscope == null){
            bloodPressureItem.setVisibility(View.GONE);
        }else{
            bloodPressureItem.setVisibility(View.VISIBLE);
            bloodPressureUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), turgoscope.getTimestamp()));
            bloodPressureData.setText(String.valueOf(turgoscope.getLowBloodPressure()) + '/' +
                                      String.valueOf(turgoscope.getHighBloodPressure()) + "Pa");
        }
    }
}

