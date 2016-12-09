package org.micronurse.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.JSONParser;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.SensorWarningListResult;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.GPS;
import org.micronurse.model.Humidometer;
import org.micronurse.model.InfraredTransducer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SensorWarning;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.Turgoscope;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class MonitorWarningFragment extends Fragment implements OnBindMQTTServiceListener {
    private static final int LIMIT_NUM = 20;

    private View viewRoot;
    private RecyclerView dataListView;
    private SwipyRefreshLayout refresh;
    private LinkedList<Object> dataList = new LinkedList<>();
    private MonitorAdapter adapter;
    private Calendar downEndTime;
    private JSONParser<SensorWarningListResult> jsonParser;
    private boolean firstDisplay = true;

    public MonitorWarningFragment() {
        // Required empty public constructor
        jsonParser = new JSONParser<SensorWarningListResult>() {
            @Override
            public SensorWarningListResult fromJson(String jsonStr) {
                Gson gson = GsonUtil.getGson();
                JsonElement rootElement = new JsonParser().parse(jsonStr);
                SensorWarningListResult result = gson.fromJson(rootElement, SensorWarningListResult.class);
                try {
                    JsonArray jsonArray = rootElement.getAsJsonObject().get("warning_list").getAsJsonArray();
                    for(int i = 0; i < jsonArray.size(); i++){
                        SensorWarning sw = result.getWarningList().get(i);
                        JsonElement sensorElement = jsonArray.get(i).getAsJsonObject().get("sensor_data");
                        switch (sw.getSensorType()) {
                            case Sensor.SENSOR_TYPE_INFRARED_TRANSDUCER:
                                sw.setSensorData(gson.fromJson(sensorElement, InfraredTransducer.class));
                                break;
                            case Sensor.SENSOR_TYPE_THERMOMETER:
                                sw.setSensorData(gson.fromJson(sensorElement, Thermometer.class));
                                break;
                            case Sensor.SENSOR_TYPE_HUMIDOMETER:
                                sw.setSensorData(gson.fromJson(sensorElement, Humidometer.class));
                                break;
                            case Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER:
                                sw.setSensorData(gson.fromJson(sensorElement, SmokeTransducer.class));
                                break;
                            case Sensor.SENSOR_TYPE_FEVER_THERMOMETER:
                                sw.setSensorData(gson.fromJson(sensorElement, FeverThermometer.class));
                                break;
                            case Sensor.SENSOR_TYPE_PULSE_TRANSDUCER:
                                sw.setSensorData(gson.fromJson(sensorElement, PulseTransducer.class));
                                break;
                            case Sensor.SENSOR_TYPE_TURGOSCOPE:
                                sw.setSensorData(gson.fromJson(sensorElement, Turgoscope.class));
                                break;
                            case Sensor.SENSOR_TYPE_GPS:
                                sw.setSensorData(gson.fromJson(sensorElement, GPS.class));
                                break;
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return result;
            }
        };
    }

    public static MonitorWarningFragment getInstance(Context context){
        return new MonitorWarningFragment();
    }

    @Override
    public void onBind(MQTTService service) {
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                    GlobalInfo.TOPIC_SENSOR_WARNING, GlobalInfo.user.getUserId(), 2, Application.ACTION_SENSOR_WARNING
            ));
        }else{
            if(GlobalInfo.guardianshipList != null){
                for(User u : GlobalInfo.guardianshipList){
                    service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                            GlobalInfo.TOPIC_SENSOR_WARNING, u.getUserId(), 2, Application.ACTION_SENSOR_WARNING
                    ));
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_monitor_warning, container, false);
        dataListView = (RecyclerView) viewRoot.findViewById(R.id.data_list);
        dataListView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MonitorAdapter(getContext(), dataList, true);
        adapter.setTxtDatatime(getString(R.string.warning_time));
        dataListView.setAdapter(adapter);
        refresh = (SwipyRefreshLayout) viewRoot.findViewById(R.id.refresh_layout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                if(direction == SwipyRefreshLayoutDirection.BOTTOM)
                    downLoadMore();
                else
                    refresh();
            }
        });
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                GlobalInfo.Guardian.monitorOlder == null) {
            refresh.setEnabled(false);
        }else{
            refresh.setDirection(SwipyRefreshLayoutDirection.TOP);
            refresh();
        }
        return viewRoot;
    }

    private void refresh(){
        downEndTime = Calendar.getInstance();
        dataList.clear();
        downLoadMore();
    }

    private void downLoadMore(){
        String url;
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            url = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.SENSOR_WARNING, String.valueOf(downEndTime.getTimeInMillis()),
                    String.valueOf(LIMIT_NUM));
        }else{
            if(GlobalInfo.Guardian.monitorOlder == null){
                refresh.setRefreshing(false);
                return;
            }
            url = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.SENSOR_WARNING, GlobalInfo.Guardian.monitorOlder.getUserId().toString(),
                    String.valueOf(downEndTime.getTimeInMillis()), String.valueOf(LIMIT_NUM));
        }
        refresh.setRefreshing(true);
        MicronurseAPI<SensorWarningListResult> request = new MicronurseAPI<SensorWarningListResult>(
                getActivity(), url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<SensorWarningListResult>() {
            @Override
            public void onResponse(SensorWarningListResult response) {
                viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
                refresh.setRefreshing(false);
                refresh.setDirection(SwipyRefreshLayoutDirection.BOTH);
                if(dataList.isEmpty()){
                    dataList.addLast(new Date(response.getWarningList().get(0).getSensorData().getTimestamp()));
                }
                for(SensorWarning sw : response.getWarningList()){
                    Date d = new Date(sw.getSensorData().getTimestamp());
                    if(dataList.getLast() instanceof Sensor && !DateTimeUtil.isSameDay(
                            new Date(((Sensor) dataList.getLast()).getTimestamp()), d)){
                        dataList.addLast(d);
                    }
                    dataList.addLast(sw.getSensorData());
                    downEndTime.setTimeInMillis(sw.getSensorData().getTimestamp() - 1);
                }
                adapter.notifyDataSetChanged();
                firstDisplay = false;
            }
        }, new APIErrorListener() {
            @Override
            public boolean onErrorResponse(VolleyError err, Result result) {
                refresh.setRefreshing(false);
                refresh.setDirection(SwipyRefreshLayoutDirection.BOTH);
                if(result != null && result.getResultCode() == PublicResultCode.SENSOR_WARNING_NOT_FOUND){
                    if(!firstDisplay)
                        Snackbar.make(viewRoot, R.string.no_more_data, Snackbar.LENGTH_SHORT).show();
                    firstDisplay = false;
                    return true;
                }
                firstDisplay = false;
                return false;
            }
        }, SensorWarningListResult.class, false, null);
        request.setJsonParser(jsonParser);
        request.startRequest();
    }
}
