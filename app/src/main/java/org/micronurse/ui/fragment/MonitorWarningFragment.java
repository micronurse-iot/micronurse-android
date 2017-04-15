package org.micronurse.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.net.DataCorruptionException;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.net.model.result.SensorWarningListResult;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.GPS;
import org.micronurse.model.Humidometer;
import org.micronurse.model.InfraredTransducer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SensorWarning;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MonitorWarningFragment extends Fragment implements OnBindMQTTServiceListener {
    private static final int LIMIT_NUM = 20;

    private View rootView;
    @BindView(R.id.warning_list)
    SuperRecyclerView warningListView;
    @BindView(R.id.txt_no_data)
    TextView txtNoData;

    private LinkedList<Object> dataList = new LinkedList<>();
    private MonitorAdapter adapter;
    private Calendar downEndTime;

    public MonitorWarningFragment() {
        // Required empty public constructor
    }

    public static MonitorWarningFragment getInstance(Context context){
        return new MonitorWarningFragment();
    }

    @Override
    public void onBind(MQTTService service) {
        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_OLDER:
                service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                        Application.MQTT_TOPIC_SENSOR_WARNING, GlobalInfo.user.getUserId(), 1, Application.ACTION_SENSOR_WARNING
                ));
                break;
            case User.ACCOUNT_TYPE_GUARDIAN:
                for(User u : GlobalInfo.guardianshipList){
                    service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                            Application.MQTT_TOPIC_SENSOR_WARNING, u.getUserId(), 1, Application.ACTION_SENSOR_WARNING
                    ));
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_monitor_warning, container, false);
        ButterKnife.bind(this, rootView);
        adapter = new MonitorAdapter(getContext(), dataList, true);

        warningListView.getRecyclerView().setLayoutManager(new LinearLayoutManager(getActivity()));
        warningListView.getRecyclerView().setNestedScrollingEnabled(false);
        warningListView.getSwipeToRefresh().setColorSchemeResources(R.color.colorAccent);
        warningListView.setRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downLoadMore(true);
            }
        });
        warningListView.setupMoreListener(new OnMoreListener() {
            @Override
            public void onMoreAsked(int numberOfItems, int numberBeforeMore, int currentItemPos) {
                downLoadMore(false);
            }
        }, -1);
        warningListView.setAdapter(adapter);
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                GlobalInfo.Guardian.monitorOlder == null) {
            warningListView.getSwipeToRefresh().setEnabled(false);
        }else{
            downLoadMore(true);
        }
        return rootView;
    }

    private void downLoadMore(final boolean refresh){
        if(refresh) {
            downEndTime = Calendar.getInstance();
            warningListView.setRefreshing(true);
        }

        String url;
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            url = HttpApi.getApiUrl(HttpApi.SensorAPI.SENSOR_WARNING, DateTimeUtil.getHttpTimestampStr(downEndTime.getTime()),
                    String.valueOf(LIMIT_NUM));
        }else{
            url = HttpApi.getApiUrl(HttpApi.SensorAPI.SENSOR_WARNING, GlobalInfo.Guardian.monitorOlder.getUserId().toString(),
                    DateTimeUtil.getHttpTimestampStr(downEndTime.getTime()), String.valueOf(LIMIT_NUM));
        }
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<SensorWarningListResult>(SensorWarningListResult.class) {
                    @Override
                    public void onResponse() {
                        warningListView.setRefreshing(false);
                        warningListView.hideMoreProgress();
                    }

                    @Override
                    public void onDataResponse(@NonNull byte[] data) throws DataCorruptionException {
                        Gson gson = GsonUtil.getGson();
                        SensorWarningListResult result;
                        try {
                            JsonElement rootElement = new JsonParser().parse(new String(data));
                            result = gson.fromJson(rootElement, SensorWarningListResult.class);
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
                                    case Sensor.SENSOR_TYPE_GPS:
                                        sw.setSensorData(gson.fromJson(sensorElement, GPS.class));
                                        break;
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                            throw new DataCorruptionException(e);
                        }
                        onDataResponse(result);
                    }

                    @Override
                    public void onDataResponse(SensorWarningListResult data) {
                        showContent(refresh, true);
                        if(refresh){
                            dataList.clear();
                            adapter.notifyDataSetChanged();
                            dataList.addLast(data.getWarningList().get(0).getSensorData().getTimestamp());
                            adapter.notifyItemInserted(0);
                        }
                        for(SensorWarning sw : data.getWarningList()){
                            Date d = sw.getSensorData().getTimestamp();
                            if(dataList.getLast() instanceof Sensor && !DateTimeUtil.isSameDay(
                                    ((Sensor) dataList.getLast()).getTimestamp(), d)){
                                dataList.addLast(d);
                                adapter.notifyItemInserted(dataList.size() - 1);
                            }
                            dataList.addLast(sw.getSensorData());
                            adapter.notifyItemInserted(dataList.size() - 1);
                            downEndTime.setTimeInMillis(sw.getSensorData().getTimestamp().getTime() - 1);
                        }
                        warningListView.setNumberBeforeMoreIsCalled((data.getWarningList().size() < LIMIT_NUM) ? -1 : 1);
                    }

                    @Override
                    public void onErrorResponse() {
                        showContent(refresh, false);
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        if(errorInfo.getResultCode() == PublicResultCode.SENSOR_WARNING_NOT_FOUND){
                            warningListView.setNumberBeforeMoreIsCalled(-1);
                            if(!refresh)
                                Snackbar.make(rootView, R.string.no_more, Snackbar.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                }));
    }

    private void showContent(boolean refresh, boolean hasContent){
        if(refresh && !hasContent){
            warningListView.getRecyclerView().setVisibility(View.INVISIBLE);
            txtNoData.setVisibility(View.VISIBLE);
        }else if(refresh){
            txtNoData.setVisibility(View.GONE);
            warningListView.getRecyclerView().setVisibility(View.VISIBLE);
        }
    }
}
