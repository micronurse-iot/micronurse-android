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

public class LocationService extends Service implements BDLocationListener {
    private final int LOCATE_INTERVAL = 5000;

    public LocationClient locationClient = null;
    private MQTTService mqttService;
    private ServiceConnection mqttServiceConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(this);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setScanSpan(LOCATE_INTERVAL);
        option.setOpenGps(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(true);
        option.setEnableSimulateGps(false);
        locationClient.setLocOption(option);
        locationClient.start();

        mqttServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mqttService = ((MQTTService.MQTTServiceBinder) service).getService();
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
                 bdLocation.getLocType() == BDLocation.TypeGpsLocation){
            sendLocation(bdLocation.getLongitude(), bdLocation.getLatitude());
        }
    }

    private void sendLocation(double longitude, double latitude) {
        longitude = 109.625072199243;
        latitude = 23.0599510614102;
        RawSensorData sensorData = new RawSensorData(Sensor.SENSOR_TYPE_GPS, System.currentTimeMillis(),
                String.valueOf(longitude) + ',' + String.valueOf(latitude));
        String message = GsonUtil.getGson().toJson(sensorData);
        Intent intent = new Intent(Application.ACTION_SENSOR_DATA_REPORT);
        intent.addCategory(getPackageName());
        intent.putExtra(Application.BUNDLE_KEY_USER_ID, GlobalInfo.user.getPhoneNumber());
        intent.putExtra(Application.BUNDLE_KEY_TOPIC, GlobalInfo.TOPIC_SENSOR_DATA_REPORT);
        intent.putExtra(Application.BUNDLE_KEY_MESSAGE, message);
        sendBroadcast(intent);
        if(mqttService == null)
            return;
        mqttService.addMQTTAction(new MQTTService.MQTTPublishAction(GlobalInfo.TOPIC_SENSOR_DATA_REPORT, GlobalInfo.user.getPhoneNumber(),
                0, message, null, null));
    }

    @Override
    public void onDestroy() {
        locationClient.stop();
        unbindService(mqttServiceConnection);
        super.onDestroy();
    }
}
