package org.micronurse.ui.activity.older.main.monitor;

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

import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.HumidometerDataListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.SmokeTransducerDataListResult;
import org.micronurse.http.model.result.ThermometerDataListResult;
import org.micronurse.model.Humidometer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.GlobalInfo;

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

    private List<Thermometer> thermometerList;
    private List<Humidometer> humidometerList;
    private List<SmokeTransducer> smokeTransducerList;

    private Timer scheduleTask;

    public FamilyMonitorFragment() {
        // Required empty public constructor
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

        return viewRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
        scheduleTask = new Timer();
        refresh.setRefreshing(true);
        scheduleTask.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTemperature();
                updateHumidity();
                updateSmoke();
            }
        }, 0, 5000);
    }

    @Override
    public void onPause() {
        scheduleTask.cancel();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        scheduleTask.cancel();
        super.onDestroy();
    }

    private void updateSafeLevel(){
        CheckUtil.checkFamilySafetyLevel(txtSafeLevel, viewRoot.findViewById(R.id.safe_level_area),
                thermometerList, humidometerList, smokeTransducerList);
    }

    private void updateTemperature(){
        new MicronurseAPI<>(getActivity(), MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA,
                Sensor.SENSOR_TYPE_THERMOMETER, String.valueOf(1)),
                Request.Method.GET, null, GlobalInfo.token, new Response.Listener<ThermometerDataListResult>() {
            @Override
            public void onResponse(ThermometerDataListResult response) {
                viewRoot.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
                viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
                viewRoot.findViewById(R.id.temperature_area).setVisibility(View.VISIBLE);
                thermometerList = response.getDataList();
                temperatureList.setAdapter(new MonitorAdapter(getActivity(), thermometerList));
                updateSafeLevel();
                refresh.setRefreshing(false);
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                refresh.setRefreshing(false);
            }
        }, ThermometerDataListResult.class, false, null).startRequest();
    }

    private void updateHumidity(){
        new MicronurseAPI<>(getActivity(), MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA,
                Sensor.SENSOR_TYPE_HUMIDOMETER, String.valueOf(1)),
                Request.Method.GET, null, GlobalInfo.token, new Response.Listener<HumidometerDataListResult>() {
                    @Override
                    public void onResponse(HumidometerDataListResult response) {
                        viewRoot.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
                        viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
                        viewRoot.findViewById(R.id.humidity_area).setVisibility(View.VISIBLE);
                        humidometerList = response.getDataList();
                        humidityList.setAdapter(new MonitorAdapter(getActivity(), humidometerList));
                        updateSafeLevel();
                        refresh.setRefreshing(false);
                    }
                }, new APIErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err, Result result) {
                        refresh.setRefreshing(false);
                    }
                }, HumidometerDataListResult.class, false, null).startRequest();
    }

    private void updateSmoke(){
        new MicronurseAPI<>(getActivity(), MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA,
                Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER, String.valueOf(1)),
                Request.Method.GET, null, GlobalInfo.token, new Response.Listener<SmokeTransducerDataListResult>() {
                    @Override
                    public void onResponse(SmokeTransducerDataListResult response) {
                        viewRoot.findViewById(R.id.safe_level_area).setVisibility(View.VISIBLE);
                        viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
                        viewRoot.findViewById(R.id.smoke_area).setVisibility(View.VISIBLE);
                        smokeTransducerList = response.getDataList();
                        smokeList.setAdapter(new MonitorAdapter(getActivity(), smokeTransducerList));
                        updateSafeLevel();
                        refresh.setRefreshing(false);
                    }
                }, new APIErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err, Result result) {
                        refresh.setRefreshing(false);
                    }
                }, SmokeTransducerDataListResult.class, false, null).startRequest();
    }
}
