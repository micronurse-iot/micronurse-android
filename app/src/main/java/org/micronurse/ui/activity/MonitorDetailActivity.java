package org.micronurse.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.FeverThermometerDataListResult;
import org.micronurse.http.model.result.HumidometerDataListResult;
import org.micronurse.http.model.result.PulseTransducerDataListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.http.model.result.SensorDataListResult;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.formatter.SimpleLineChartValueFormatter;
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
    private static final int MAX_POINT_NUM = 15;

    private TextView txtDataTime;
    private TextView txtData;
    private LineChartView lineChart;

    private String sensorType;
    private String name;
    private Class resultType;

    private long startTimestamp = 0;
    private Timer scheduleTask;
    private LinkedList<Sensor> dataList = new LinkedList<>();
    private String url;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    private int pointNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        sensorType = intent.getStringExtra(BUNDLE_KEY_SENSOR_TYPE);
        name = intent.getStringExtra(BUNDLE_KEY_SENSOR_NAME);
        switch (sensorType) {
            case Sensor.SENSOR_TYPE_THERMOMETER:
                resultType = ThermometerDataListResult.class;
                break;
            case Sensor.SENSOR_TYPE_HUMIDOMETER:
                resultType = HumidometerDataListResult.class;
                break;
            case Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER:
                resultType = SmokeTransducerDataListResult.class;
                break;
            case Sensor.SENSOR_TYPE_FEVER_THERMOMETER:
                resultType = FeverThermometerDataListResult.class;
                break;
            case Sensor.SENSOR_TYPE_PULSE_TRANSDUCER:
                resultType = PulseTransducerDataListResult.class;
                break;
            case Sensor.SENSOR_TYPE_TURGOSCOPE:
                resultType = TurgoscopeDataListResult.class;
                break;
        }

        ((TextView) findViewById(R.id.data_item).findViewById(R.id.data_name)).setText(name);
        txtDataTime = (TextView) findViewById(R.id.data_item).findViewById(R.id.data_update_time);
        txtData = (TextView) findViewById(R.id.data_item).findViewById(R.id.data);
        lineChart = (LineChartView) findViewById(R.id.line_chart);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scheduleTask = new Timer();
        scheduleTask.schedule(new TimerTask() {
            @Override
            public void run() {
                final long endTimestamp = System.currentTimeMillis();
                int limitNum = (startTimestamp <= 0) ? MAX_POINT_NUM : 0;
                try {
                    if (GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
                        url = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, sensorType,
                                URLEncoder.encode(name, "utf-8"), String.valueOf(startTimestamp), String.valueOf(endTimestamp), String.valueOf(limitNum));
                    else
                        url = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA, String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()), sensorType,
                                URLEncoder.encode(name, "utf-8"), String.valueOf(startTimestamp), String.valueOf(endTimestamp), String.valueOf(limitNum));
                } catch (UnsupportedEncodingException uee) {
                    uee.printStackTrace();
                    return;
                }
                new MicronurseAPI(MonitorDetailActivity.this, url, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<SensorDataListResult>() {
                    @Override
                    public void onResponse(SensorDataListResult response) {
                        startTimestamp = endTimestamp + 1000;
                        dataList.addAll(0, response.getDataList());
                        while(dataList.size() > MAX_POINT_NUM)
                            dataList.removeLast();
                        updateSensorData();
                    }
                }, new APIErrorListener() {
                    @Override
                    public boolean onErrorResponse(VolleyError err, Result result) {
                        if(result != null && result.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND)
                            return true;
                        return false;
                    }
                }, resultType, false, null).startRequest();
            }
        }, 0, 5000);
    }


    @Override
    protected void onPause() {
        scheduleTask.cancel();
        super.onPause();
    }

    @SuppressLint("SetTextI18n")
    private void updateSensorData(){
        if(dataList.isEmpty())
            return;
        txtDataTime.setText(DateTimeUtil.convertTimestamp(MonitorDetailActivity.this, dataList.get(0).getTimestamp()));

        if (pointNum > 0) {
            mAxisXValues.clear();
            mPointValues.clear();
        }
        pointNum = dataList.size();
        for (int i = 0; i <  pointNum; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(DateTimeUtil.convertTimestamp(this, dataList.get(pointNum - i - 1).getTimestamp())));
        }

        switch (sensorType) {
            case Sensor.SENSOR_TYPE_THERMOMETER:
                txtData.setText(String.valueOf(((Thermometer)dataList.get(0)).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel((Thermometer)dataList.get(0));
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((Thermometer) dataList.get(pointNum - i - 1)).getTemperature()));
                }
                initLineChart("  ",R.color.orange_500, -100, 100);
                break;
            case Sensor.SENSOR_TYPE_HUMIDOMETER:
                txtData.setText(String.valueOf(((Humidometer)dataList.get(0)).getHumidity()) + '%');
                CheckUtil.checkSafetyLevel((Humidometer) dataList.get(0));
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((Humidometer) dataList.get(pointNum - i - 1)).getHumidity()));
                }
                initLineChart(getString(R.string.humidity_unit),R.color.orange_500, 0, 100);
                break;
            case Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER:
                txtData.setText(String.valueOf(((SmokeTransducer)dataList.get(0)).getSmoke()) + "ppm");
                CheckUtil.checkSafetyLevel((SmokeTransducer) dataList.get(0));
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((SmokeTransducer) dataList.get(pointNum - i - 1)).getSmoke()));
                }
                initLineChart( getString(R.string.smoke), R.color.orange_500, 0, 100);
                break;
            case Sensor.SENSOR_TYPE_FEVER_THERMOMETER:
                txtData.setText(String.valueOf(((FeverThermometer)dataList.get(0)).getTemperature()) + "°C");
                CheckUtil.checkSafetyLevel((FeverThermometer) dataList.get(0));
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((FeverThermometer) dataList.get(pointNum - i - 1)).getTemperature()));
                }
                initLineChart("  ", R.color.orange_500, 20, 50);
                break;
            case Sensor.SENSOR_TYPE_PULSE_TRANSDUCER:
                txtData.setText(String.valueOf(((PulseTransducer)dataList.get(0)).getPulse()) + "bpm");
                CheckUtil.checkSafetyLevel((PulseTransducer) dataList.get(0));
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((PulseTransducer) dataList.get(pointNum - i - 1)).getPulse()));
                }
                initLineChart(getString(R.string.pulse_unit) , R.color.orange_500, 0, 200);
                break;
            case Sensor.SENSOR_TYPE_TURGOSCOPE:
                txtData.setText(String.valueOf(((Turgoscope)dataList.get(0)).getLowBloodPressure()) + '/' +
                        String.valueOf(((Turgoscope)dataList.get(0)).getHighBloodPressure()) + "Pa");
                CheckUtil.checkSafetyLevel((Turgoscope) dataList.get(0));
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((Turgoscope) dataList.get(pointNum - i - 1)).getHighBloodPressure()));
                }
                initLineChart(getString(R.string.high_low_blood_pleasure), R.color.orange_500, 0, 200);
                //mPointValues.clear();
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((Turgoscope) dataList.get(pointNum - i - 1)).getLowBloodPressure()));
                }
                initLineChart(getString(R.string.high_low_blood_pleasure), R.color.blue_500, 0, 200);
                break;
        }

        lineChart.postInvalidate();
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


    private void initLineChart(String yName, int color, int yBottom, int yTop) {
        Line line = new Line(mPointValues).setColor(color);
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);
        line.setFilled(false);
        // line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        line.setFormatter(new SimpleLineChartValueFormatter(1));
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.BLUE);  //设置字体颜色
        axisX.setName("\n\n时间");  //表格名称
        axisX.setTextSize(15);//设置字体大小
        axisX.setMaxLabelChars(15); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        axisX.setHasLines(true); //x 轴分割线

        Axis axisY = new Axis();  //Y轴
        axisY.setName(yName);//y轴标注
        axisY.setTextSize(15);//设置字体大小
        axisY.setTextColor(Color.BLUE);
        data.setAxisYLeft(axisY);  //Y轴设置在左边

        axisY.setHasLines(true);

        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 2);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);
        final Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        v.bottom = yBottom;
        v.top = yTop;
        lineChart.setCurrentViewport(v);
        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
    }
}