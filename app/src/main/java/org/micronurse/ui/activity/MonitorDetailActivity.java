package org.micronurse.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.Humidometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.Turgoscope;
import org.micronurse.model.User;
import org.micronurse.util.CheckUtil;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class MonitorDetailActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_SENSOR_TYPE = "sensor_type";
    public static final String BUNDLE_KEY_SENSOR_NAME = "sensor_name";

    private TextView txtDataTime;
    private TextView txtData;
    private LineChartView lineChart;

    private String sensorType;
    private String name;
    private Calendar startTime;
    private Timer scheduleTask;
    private List dataList;
    private String url;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    private int pointNum = 0;
    private static int MAX_POINT_NUM = 20;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        sensorType = intent.getStringExtra(BUNDLE_KEY_SENSOR_TYPE);
        name = intent.getStringExtra(BUNDLE_KEY_SENSOR_NAME);

        ((TextView) findViewById(R.id.data_item).findViewById(R.id.data_name)).setText(name);
        txtDataTime = (TextView) findViewById(R.id.data_item).findViewById(R.id.data_update_time);
        txtData = (TextView) findViewById(R.id.data_item).findViewById(R.id.data);

        startTime = Calendar.getInstance();
        startTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE) - 1);

        lineChart = (LineChartView) findViewById(R.id.line_chart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleTask = new Timer();
        scheduleTask.schedule(new TimerTask() {
            @Override
            public void run() {
                if (sensorType.equals(Sensor.SENSOR_TYPE_THERMOMETER))
                    updateThermometer();
                else if (sensorType.equals(Sensor.SENSOR_TYPE_HUMIDOMETER))
                    updateHumidometer();
                else if (sensorType.equals(Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER))
                    updateSmokeTransducer();
                else if (sensorType.equals(Sensor.SENSOR_TYPE_FEVER_THERMOMETER))
                    updateFeverThermometer();
                else if (sensorType.equals(Sensor.SENSOR_TYPE_PULSE_TRANSDUCER))
                    updatePulseTransducer();
                else if (sensorType.equals(Sensor.SENSOR_TYPE_TURGOSCOPE))
                    updateTurgoscope();
            }
        }, 0, 5000);
    }

    @Override
    protected void onPause() {
        scheduleTask.cancel();
        super.onPause();
    }

    private void updateURL() {
        try {
            if (GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
                url = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, sensorType,
                        URLEncoder.encode(name, "utf-8"), String.valueOf(startTime.getTimeInMillis()), String.valueOf(System.currentTimeMillis()), String.valueOf(0));
            else
                url = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA, GlobalInfo.Guardian.monitorOlder.getPhoneNumber(), sensorType,
                        URLEncoder.encode(name, "utf-8"), String.valueOf(startTime.getTimeInMillis()), String.valueOf(System.currentTimeMillis()), String.valueOf(0));
        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    private void updateLatestDataTime() {
        if (dataList != null && !dataList.isEmpty())
            txtDataTime.setText(DateTimeUtil.convertTimestamp(this, ((Sensor) dataList.get(0)).getTimestamp()));
    }

    @SuppressLint("SetTextI18n")
    private void updateThermometer() {
        updateURL();
        new MicronurseAPI<ThermometerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<ThermometerDataListResult>() {
            @Override
            public void onResponse(ThermometerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));

                if (dataList != null && !dataList.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ( pointNum > 0) {
                        mAxisXValues.clear();
                        mPointValues.clear();
                    }
                    pointNum = dataList.size();
                    if(pointNum > MAX_POINT_NUM){
                        pointNum = MAX_POINT_NUM;
                    }
                    for (int i = 0; i <  pointNum; i++) {
                        mAxisXValues.add(new AxisValue(i).setLabel(sdf.format(((Sensor) dataList.get(pointNum - i - 1)).getTimestamp())));
                    }
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((Thermometer) dataList.get(pointNum - i - 1)).getTemperature()));
                    }
                    lineChart.postInvalidate();
                    initLineChart(((Thermometer) dataList.get(0)).getName(), "温度/°C",R.color.orange_500);
                }

            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, ThermometerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateHumidometer() {
        updateURL();
        new MicronurseAPI<HumidometerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<HumidometerDataListResult>() {
            @Override
            public void onResponse(HumidometerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getHumidity()) + '%');
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));

                if (dataList != null && !dataList.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ( pointNum > 0) {
                        mAxisXValues.clear();
                        mPointValues.clear();
                    }
                    pointNum = dataList.size();
                    if(pointNum > MAX_POINT_NUM){
                        pointNum = MAX_POINT_NUM;
                    }
                    for (int i = 0; i <  pointNum; i++) {
                        mAxisXValues.add(new AxisValue(i).setLabel(sdf.format(((Sensor) dataList.get(pointNum - i - 1)).getTimestamp())));
                    }
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((Humidometer) dataList.get(pointNum - i - 1)).getHumidity()));
                    }
                    lineChart.postInvalidate();
                    initLineChart(((Humidometer) dataList.get(0)).getName(), "湿度/%",R.color.orange_500);
                }
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, HumidometerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateSmokeTransducer() {
        updateURL();
        new MicronurseAPI<SmokeTransducerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<SmokeTransducerDataListResult>() {
            @Override
            public void onResponse(SmokeTransducerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getSmoke()));
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));

                if (dataList != null && !dataList.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ( pointNum > 0) {
                        mAxisXValues.clear();
                        mPointValues.clear();
                    }
                    pointNum = dataList.size();
                    if(pointNum > MAX_POINT_NUM){
                        pointNum = MAX_POINT_NUM;
                    }
                    for (int i = 0; i <  pointNum; i++) {
                        mAxisXValues.add(new AxisValue(i).setLabel(sdf.format(((Sensor) dataList.get(pointNum - i - 1)).getTimestamp())));
                    }
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((SmokeTransducer) dataList.get(pointNum - i - 1)).getSmoke()));
                    }
                    lineChart.postInvalidate();
                    initLineChart(((SmokeTransducer) dataList.get(0)).getName(), "浓度", R.color.orange_500);
                }
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, SmokeTransducerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateFeverThermometer() {
        updateURL();
        new MicronurseAPI<FeverThermometerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<FeverThermometerDataListResult>() {
            @Override
            public void onResponse(FeverThermometerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));

                if (dataList != null && !dataList.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ( pointNum > 0) {
                        mAxisXValues.clear();
                        mPointValues.clear();
                    }
                    pointNum = dataList.size();
                    if(pointNum > MAX_POINT_NUM){
                        pointNum = MAX_POINT_NUM;
                    }
                    for (int i = 0; i <  pointNum; i++) {
                        mAxisXValues.add(new AxisValue(i).setLabel(sdf.format(((Sensor) dataList.get(pointNum - i - 1)).getTimestamp())));
                    }
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((FeverThermometer) dataList.get(pointNum - i - 1)).getTemperature()));
                    }
                    lineChart.postInvalidate();
                    initLineChart(getString(R.string.fever), getString(R.string.temperature_unit), R.color.orange_500);
                }

            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, FeverThermometerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updatePulseTransducer() {
        updateURL();
        new MicronurseAPI<PulseTransducerDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<PulseTransducerDataListResult>() {
            @Override
            public void onResponse(PulseTransducerDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getPulse()) + "bpm");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));

                if (dataList != null && !dataList.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ( pointNum > 0) {
                        mAxisXValues.clear();
                        mPointValues.clear();
                    }
                    pointNum = dataList.size();
                    if(pointNum > MAX_POINT_NUM){
                        pointNum = MAX_POINT_NUM;
                    }
                    for (int i = 0; i <  pointNum; i++) {
                        mAxisXValues.add(new AxisValue(i).setLabel(sdf.format(((Sensor) dataList.get(pointNum - i - 1)).getTimestamp())));
                    }
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((PulseTransducer) dataList.get(pointNum - i - 1)).getPulse()));
                    }
                    lineChart.postInvalidate();
                    initLineChart(getString(R.string.pulse),getString(R.string.pulse_unit) , R.color.orange_500);
                }
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {

            }
        }, PulseTransducerDataListResult.class, false, null).startRequest();
    }

    @SuppressLint("SetTextI18n")
    private void updateTurgoscope() {
        updateURL();
        new MicronurseAPI<TurgoscopeDataListResult>(this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<TurgoscopeDataListResult>() {
            @Override
            public void onResponse(TurgoscopeDataListResult response) {
                dataList = response.getDataList();
                updateLatestDataTime();
                txtData.setText(String.valueOf(response.getDataList().get(0).getLowBloodPressure()) + '/' +
                        String.valueOf(response.getDataList().get(0).getHighBloodPressure()) + "Pa");
                CheckUtil.checkSafetyLevel(txtData, response.getDataList().get(0));

                if (dataList != null && !dataList.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    if ( pointNum > 0) {
                        mAxisXValues.clear();
                        mPointValues.clear();
                    }
                    pointNum = dataList.size();
                    if(pointNum > MAX_POINT_NUM){
                        pointNum = MAX_POINT_NUM;
                    }
                    for (int i = 0; i <  pointNum; i++) {
                        mAxisXValues.add(new AxisValue(i).setLabel(sdf.format(((Sensor) dataList.get(pointNum - i - 1)).getTimestamp())));
                    }
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((Turgoscope) dataList.get(pointNum - i - 1)).getHighBloodPressure()));
                    }
                    lineChart.postInvalidate();
                    initLineChart(getString(R.string.blood_pressure), "高压/低压", R.color.orange_500);
                    for (int i = 0; i < pointNum; i++) {
                        mPointValues.add(new PointValue(i, ((Turgoscope) dataList.get(pointNum - i - 1)).getLowBloodPressure()));
                    }
                    initLineChart(getString(R.string.blood_pressure), "高压/低压", R.color.blue_500);
                }
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


    private void initLineChart(String tableName, String yName, int color) {
        Line line = new Line(mPointValues).setColor(color);
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);
        line.setFilled(false);
        // line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.BLUE);  //设置字体颜色
        axisX.setName(tableName);  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(20); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        axisX.setHasLines(true); //x 轴分割线

        Axis axisY = new Axis();  //Y轴
        axisY.setName(yName);//y轴标注
        axisY.setTextSize(10);//设置字体大小
        axisY.setTextColor(Color.BLUE);
        data.setAxisYLeft(axisY);  //Y轴设置在左边


        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        lineChart.setCurrentViewport(v);
    }
}