package org.micronurse;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    public static String ACTION_SENSOR_DATA_REPORT;
    public static String ACTION_SENSOR_WARNING;

    public static final String BUNDLE_KEY_RAW_SENSOR_DATA = "RawSensorData";
    public static final String BUNDLE_KEY_USER_ID = "UserId";

    @Override
    public void onCreate() {
        super.onCreate();
        //Init Baidu Map SDK
        SDKInitializer.initialize(this);
        ACTION_SENSOR_DATA_REPORT = getPackageName() + ".action.SENSOR_DATA_REPORT";
        ACTION_SENSOR_WARNING = getPackageName() + ".action.SENSOR_WARNING";
    }
}
