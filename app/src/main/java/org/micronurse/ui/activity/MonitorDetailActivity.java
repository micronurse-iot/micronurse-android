package org.micronurse.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.FeverThermometerDataListResult;
import org.micronurse.http.model.result.HumidometerDataListResult;
import org.micronurse.http.model.result.PulseTransducerDataListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.SmokeTransducerDataListResult;
import org.micronurse.http.model.result.ThermometerDataListResult;
import org.micronurse.http.model.result.TurgoscopeDataListResult;
import org.micronurse.model.Sensor;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorDetailActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_SENSOR_TYPE = "sensor_type";
    public static final String BUNDLE_KEY_SENSOR_NAME = "sensor_name";

    private TextView txtDataTime;
    private TextView txtData;

    private String sensorType;
    private String name;
    private Calendar startTime;
    private Timer scheduleTask;
    private List dataList;
    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        sensorType = intent.getStringExtra(BUNDLE_KEY_SENSOR_TYPE);
        name = intent.getStringExtra(BUNDLE_KEY_SENSOR_NAME);

        ((TextView)findViewById(R.id.data_item).findViewById(R.id.data_name)).setText(name);
        txtDataTime = (TextView)findViewById(R.id.data_item).findViewById(R.id.data_update_time);
        txtData = (TextView)findViewById(R.id.data_item).findViewById(R.id.data);

        startTime = Calendar.getInstance();
        //TODO: Set a proper start time
        startTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE) - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleTask = new Timer();
        scheduleTask.schedule(new TimerTask() {
            @Override
            public void run() {
                if(sensorType.equals(Sensor.SENSOR_TYPE_THERMOMETER))
                    updateThermometer();
                else if(sensorType.equals(Sensor.SENSOR_TYPE_HUMIDOMETER))
                    updateHumidometer();
                else if(sensorType.equals(Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER))
                    updateSmokeTransducer();
                else if(sensorType.equals(Sensor.SENSOR_TYPE_FEVER_THERMOMETER))
                    updateFeverThermometer();
                else if(sensorType.equals(Sensor.SENSOR_TYPE_PULSE_TRANSDUCER))
                    updatePulseTransducer();
                else if(sensorType.equals(Sensor.SENSOR_TYPE_TURGOSCOPE))
                    updateTurgoscope();
            }
        }, 0, 5000);
    }

    @Override
    protected void onPause() {
        scheduleTask.cancel();
        super.onPause();
    }

    private void updateURL(){
        try {
            if (GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
                url = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, sensorType,
                        URLEncoder.encode(name, "utf-8"), String.valueOf(startTime.getTimeInMillis()), String.valueOf(System.currentTimeMillis()), String.valueOf(0));
            else
                url = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA, GlobalInfo.Guardian.monitorOlder.getPhoneNumber(), sensorType,
                        URLEncoder.encode(name, "utf-8"), String.valueOf(startTime.getTimeInMillis()), String.valueOf(System.currentTimeMillis()), String.valueOf(0));
        }catch (UnsupportedEncodingException uee){
            uee.printStackTrace();
        }
    }

    private void updateLatestDataTime(){
        if(dataList != null && !dataList.isEmpty())
            txtDataTime.setText(DateTimeUtil.convertTimestamp(this, ((Sensor)dataList.get(0)).getTimestamp()));
    }

    @SuppressLint("SetTextI18n")
    private void updateThermometer(){
        updateURL();
        new MicronurseAPI<ThermometerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<ThermometerDataListResult>() {
            @Override
            public void onResponse(ThermometerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));
                //TODO
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, ThermometerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateHumidometer(){
        updateURL();
        new MicronurseAPI<HumidometerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<HumidometerDataListResult>() {
            @Override
            public void onResponse(HumidometerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getHumidity()) + '%');
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));
                //TODO
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, HumidometerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateSmokeTransducer(){
        updateURL();
        new MicronurseAPI<SmokeTransducerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<SmokeTransducerDataListResult>() {
            @Override
            public void onResponse(SmokeTransducerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getSmoke()));
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));
                //TODO
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, SmokeTransducerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateFeverThermometer(){
        updateURL();
        new MicronurseAPI<FeverThermometerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<FeverThermometerDataListResult>() {
            @Override
            public void onResponse(FeverThermometerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));
                //TODO
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, FeverThermometerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updatePulseTransducer(){
        updateURL();
        new MicronurseAPI<PulseTransducerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<PulseTransducerDataListResult>() {
            @Override
            public void onResponse(PulseTransducerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getPulse()) + "bpm");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));
                //TODO
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, PulseTransducerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateTurgoscope(){
        updateURL();
        new MicronurseAPI<TurgoscopeDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<TurgoscopeDataListResult>() {
            @Override
            public void onResponse(TurgoscopeDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getLowBloodPressure()) + '/' +
                                String.valueOf(response.getDataList().get(0).getHighBloodPressure()) + "Pa");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));
                //TODO
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, TurgoscopeDataListResult.class, false, null).startRequest();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
