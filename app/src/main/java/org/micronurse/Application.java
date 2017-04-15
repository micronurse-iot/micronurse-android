package org.micronurse;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.baidu.mapapi.SDKInitializer;

import org.micronurse.net.http.HttpApi;
import org.micronurse.service.MQTTService;
import org.micronurse.util.SharedPreferenceUtil;

import java.util.ArrayList;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    public static String ACTION_SENSOR_DATA_REPORT;
    public static String ACTION_SENSOR_WARNING;
    public static String ACTION_MEDICATION_REMINDER;
    public static String ACTION_CHAT_MESSAGE_SENT;
    public static String ACTION_CHAT_MESSAGE_SENT_SAVED;
    public static String ACTION_CHAT_MESSAGE_RECEIVED;
    public static String ACTION_CHAT_MESSAGE_SEND_START;
    public static String ACTION_CHAT_MESSAGE_RECEIVED_SAVED;
    public static String ACTION_SESSION_UPDATE;

    public static final String BUNDLE_KEY_CHAT_MSG_DB_ID = "MessageDBID";
    public static final String BUNDLE_KEY_SESSION_DB_ID = "SessionDBID";

    public static String MQTT_TOPIC_SENSOR_DATA_REPORT = "sensor_data_report";
    public static String MQTT_TOPIC_SENSOR_WARNING = "sensor_warning";
    public static String MQTT_TOPIC_CHATTING_FRIEND = "chatting_friend";
    public static String MQTT_TOPIC_CHATTING_GUARDIANSHIP = "chatting_guardianship";

    @Override
    public void onCreate() {
        super.onCreate();
        //Init Baidu Map SDK
        SDKInitializer.initialize(this);
        //Init Dev Preference
        SharedPreferenceUtil.openDevPreference(this);

        MQTTService.BROKER_URL = "tcp://" +
                SharedPreferenceUtil.getDevPreference().getString(SharedPreferenceUtil.DEV_KEY_MQTTBROKER_HOST, "localhost")
                + ":13883";
        HttpApi.BASE_URL_V1 = "http://" + SharedPreferenceUtil.getDevPreference().getString(SharedPreferenceUtil.DEV_KEY_WEBSERVER_HOST, "localhost") + ":13000/micronurse/v1/mobile";

        ACTION_SENSOR_DATA_REPORT = getPackageName() + ".action.SENSOR_DATA_REPORT";
        ACTION_SENSOR_WARNING = getPackageName() + ".action.SENSOR_WARNING";
        ACTION_CHAT_MESSAGE_SENT = getPackageName() + ".action.CHAT_MESSAGE_SENT";
        ACTION_CHAT_MESSAGE_RECEIVED = getPackageName() + ".action.CHAT_MESSAGE_RECEIVED";
        ACTION_CHAT_MESSAGE_SEND_START = getPackageName() + ".action.CHAT_MESSAGE_SEND_START";
        ACTION_CHAT_MESSAGE_SENT_SAVED = getPackageName() + ".action.CHAT_MESSAGE_SENT_SAVED";
        ACTION_CHAT_MESSAGE_RECEIVED_SAVED = getPackageName() + ".action.CHAT_MESSAGE_RECEIVED_SAVED";
        ACTION_SESSION_UPDATE = getPackageName() + ".action.SESSION_UPDATE";
        ACTION_MEDICATION_REMINDER = getPackageName() + ".action.MEDICATION_REMINDER";
    }

    public static void checkPermission(Activity activity, String[] permissions){
        ArrayList<String> permCheckList = new ArrayList<>();
        for(String perm : permissions){
            if (ContextCompat.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                    continue;
                }
                permCheckList.add(perm);
            }
        }
        if(permCheckList.isEmpty())
            return;
        ActivityCompat.requestPermissions(activity, permCheckList.toArray(new String[permCheckList.size()]), 0);
    }
}
