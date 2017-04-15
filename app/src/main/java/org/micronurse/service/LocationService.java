package org.micronurse.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.micronurse.Application;
import org.micronurse.model.RawSensorData;
import org.micronurse.model.Sensor;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

import java.util.Date;

public class LocationService extends Service implements BDLocationListener {
    private final int LOCATE_INTERVAL = 5000;
    private final int SEND_INTERVAL = LOCATE_INTERVAL * 2;

    public LocationClient locationClient = null;
    private MQTTService mqttService;
    private ServiceConnection mqttServiceConnection;
    private long sendTimestamp = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(this);

        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(LOCATE_INTERVAL);
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(true);
        option.setEnableSimulateGps(false);
        option.setIsNeedLocationDescribe(true);
        locationClient.setLocOption(option);
        locationClient.start();

        mqttServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mqttService = ((MQTTService.Binder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mqttService = null;
            }
        };
        bindService(new Intent(this, MQTTService.class), mqttServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation.getLocType() == BDLocation.TypeServerError) {
            Log.e(GlobalInfo.LOG_TAG, "Locate error due to server error");
        }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkException){
            Log.e(GlobalInfo.LOG_TAG, "Locate error due to network exception");
        }else if(bdLocation.getLocType() == BDLocation.TypeCriteriaException){
            Log.e(GlobalInfo.LOG_TAG, "Locate error due to criteria exception");
        }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation ||
                 bdLocation.getLocType() == BDLocation.TypeGpsLocation ||
                 bdLocation.getLocType() == BDLocation.TypeOffLineLocation){
            sendLocation(bdLocation.getLongitude(), bdLocation.getLatitude(), bdLocation.getLocationDescribe());
        }
    }

    private void sendLocation(double longitude, double latitude, String addr) {
        if(GlobalInfo.user == null)
            return;
        RawSensorData sensorData = new RawSensorData(Sensor.SENSOR_TYPE_GPS, new Date(),
                String.valueOf(longitude) + ',' + String.valueOf(latitude) + ',' + addr);
        String message = GsonUtil.getGson().toJson(sensorData);
        Intent intent = new Intent(Application.ACTION_SENSOR_DATA_REPORT);
        intent.addCategory(getPackageName());
        intent.putExtra(MQTTService.BUNDLE_KEY_TOPIC_OWNER_ID, GlobalInfo.user.getUserId());
        intent.putExtra(MQTTService.BUNDLE_KEY_TOPIC, Application.MQTT_TOPIC_SENSOR_DATA_REPORT);
        intent.putExtra(MQTTService.BUNDLE_KEY_MESSAGE, message);
        sendBroadcast(intent);
        if(mqttService == null || System.currentTimeMillis() - sendTimestamp < SEND_INTERVAL)
            return;
        sendTimestamp = System.currentTimeMillis();
        mqttService.addMQTTAction(new MQTTService.MQTTPublishAction(Application.MQTT_TOPIC_SENSOR_DATA_REPORT, GlobalInfo.user.getUserId(),
                0, message, null));
    }

    @Override
    public void onDestroy() {
        locationClient.stop();
        unbindService(mqttServiceConnection);
        super.onDestroy();
    }
}
