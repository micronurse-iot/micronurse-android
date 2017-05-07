package org.micronurse.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.google.gson.JsonSyntaxException;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.adapter.MonitorAdapter;
import org.micronurse.model.RawSensorData;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.FeverThermometerDataListResult;
import org.micronurse.net.model.result.HumidometerDataListResult;
import org.micronurse.net.model.result.PulseTransducerDataListResult;
import org.micronurse.net.model.result.SensorDataListResult;
import org.micronurse.net.model.result.SmokeTransducerDataListResult;
import org.micronurse.net.model.result.ThermometerDataListResult;
import org.micronurse.model.FeverThermometer;
import org.micronurse.model.Humidometer;
import org.micronurse.model.PulseTransducer;
import org.micronurse.model.Sensor;
import org.micronurse.model.SmokeTransducer;
import org.micronurse.model.Thermometer;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.listener.OnSensorDataReceivedListener;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;
import org.micronurse.util.SensorUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
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

public class MonitorDetailActivity extends AppCompatActivity implements OnSensorDataReceivedListener {
    public static final String BUNDLE_KEY_SENSOR_TYPE = "sensor_type";
    public static final String BUNDLE_KEY_SENSOR_NAME = "sensor_name";
    private static final int MAX_POINT_NUM = 15;

    @BindView(R.id.data_item)
    View sensorDataItem;
    @BindView(R.id.line_chart)
    LineChartView lineChart;

    private String sensorType;
    private String sensorName;
    private Class resultType;
    private SensorDataReceiver sensorDataReceiver;

    private LinkedList<Sensor> dataList = new LinkedList<>();
    private String url;
    private List<PointValue> mPointValues = new ArrayList<PointValue>();
    private List<AxisValue> mAxisXValues = new ArrayList<AxisValue>();

    private int pointNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_detail);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        sensorType = intent.getStringExtra(BUNDLE_KEY_SENSOR_TYPE);
        sensorName = intent.getStringExtra(BUNDLE_KEY_SENSOR_NAME);
        setTitle(sensorName);
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
        }

        try {
            switch (GlobalInfo.user.getAccountType()) {
                case User.ACCOUNT_TYPE_OLDER:
                    switch (sensorType){
                        case Sensor.SENSOR_TYPE_THERMOMETER:
                        case Sensor.SENSOR_TYPE_HUMIDOMETER:
                        case Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER:
                            url = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA_BY_NAME,
                                    sensorType, URLEncoder.encode(sensorName, "utf-8"), String.valueOf(MAX_POINT_NUM));
                            break;
                        default:
                            url = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                                    sensorType, String.valueOf(MAX_POINT_NUM));
                            break;
                    }
                    break;
                case User.ACCOUNT_TYPE_GUARDIAN:
                    switch (sensorType){
                        case Sensor.SENSOR_TYPE_THERMOMETER:
                        case Sensor.SENSOR_TYPE_HUMIDOMETER:
                        case Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER:
                            url = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA_BY_NAME,
                                    String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()), sensorType,
                                    URLEncoder.encode(sensorName, "utf-8"), String.valueOf(MAX_POINT_NUM));
                            break;
                        default:
                            url = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                                    String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                                    sensorType, String.valueOf(MAX_POINT_NUM));
                            break;
                    }
                    break;
                default:
                    return;
            }
        }catch (UnsupportedEncodingException uee){
            uee.printStackTrace();
            return;
        }


        HttpApi.startRequest(new HttpApiJsonRequest(this, url, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<SensorDataListResult>(resultType) {
                    @Override
                    public void onDataResponse(SensorDataListResult data) {
                        dataList.addAll(data.getDataList());
                        updateSensorData();
                        IntentFilter filter = new IntentFilter(Application.ACTION_SENSOR_DATA_REPORT);
                        filter.addCategory(getPackageName());
                        sensorDataReceiver = new SensorDataReceiver();
                        registerReceiver(sensorDataReceiver, filter);
                    }
                }));
    }

    @SuppressLint("SetTextI18n")
    private void updateSensorData(){
        if(dataList.isEmpty())
            return;
        MonitorAdapter.bindViewHolder(new MonitorAdapter.SensorItemViewHolder(sensorDataItem), dataList.get(0));

        if (pointNum > 0) {
            mAxisXValues.clear();
            mPointValues.clear();
        }
        pointNum = dataList.size();
        for (int i = 0; i < pointNum; i++) {
            mAxisXValues.add(new AxisValue(i).setLabel(DateTimeUtil.convertTimestamp(this, dataList.get(pointNum - i - 1).getTimestamp(), true, true, true)));
        }

        switch (sensorType) {
            case Sensor.SENSOR_TYPE_THERMOMETER:
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((Thermometer) dataList.get(pointNum - i - 1)).getTemperature()));
                }
                initLineChart("  ",R.color.orange_500, -100, 100);
                break;
            case Sensor.SENSOR_TYPE_HUMIDOMETER:
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((Humidometer) dataList.get(pointNum - i - 1)).getHumidity()));
                }
                initLineChart(getString(R.string.humidity_unit),R.color.orange_500, 0, 100);
                break;
            case Sensor.SENSOR_TYPE_SMOKE_TRANSDUCER:
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((SmokeTransducer) dataList.get(pointNum - i - 1)).getSmoke()));
                }
                initLineChart(getString(R.string.smoke), R.color.orange_500, 0, 100);
                break;
            case Sensor.SENSOR_TYPE_FEVER_THERMOMETER:
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((FeverThermometer) dataList.get(pointNum - i - 1)).getTemperature()));
                }
                initLineChart("  ", R.color.orange_500, 20, 50);
                break;
            case Sensor.SENSOR_TYPE_PULSE_TRANSDUCER:
                for (int i = 0; i < pointNum; i++) {
                    mPointValues.add(new PointValue(i, ((PulseTransducer) dataList.get(pointNum - i - 1)).getPulse()));
                }
                initLineChart(getString(R.string.pulse_unit) , R.color.orange_500, 0, 200);
                break;
        }

        lineChart.postInvalidate();
    }

    @Override
    protected void onDestroy() {
        if(sensorDataReceiver != null)
            unregisterReceiver(sensorDataReceiver);
        super.onDestroy();
    }

    @Override
    public void onSensorDataReceived(Sensor sensor) {
        if(sensor == null)
            return;
        dataList.removeLast();
        dataList.addFirst(sensor);
        updateSensorData();
    }

    private class SensorDataReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent mqttIntent) {
            if(GlobalInfo.user == null)
                return;
            long userId = mqttIntent.getLongExtra(MQTTService.BUNDLE_KEY_TOPIC_OWNER_ID, -1);
            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER &&
                    GlobalInfo.user.getUserId() != userId)
                return;
            else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                    (GlobalInfo.Guardian.monitorOlder == null || GlobalInfo.Guardian.monitorOlder.getUserId() != userId))
                return;
            try {
                RawSensorData rawSensorData = GsonUtil.getGson().fromJson(mqttIntent.getStringExtra(MQTTService.BUNDLE_KEY_MESSAGE), RawSensorData.class);
                if(rawSensorData.getSensorType() != null && sensorType.equals(rawSensorData.getSensorType().toLowerCase()))
                    onSensorDataReceived(SensorUtil.parseRawSensorData(rawSensorData));
            }catch (JsonSyntaxException e){
                e.printStackTrace();
            }
        }
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
        axisX.setName(getString(R.string.linechart_xlabel));  //表格名称
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