package org.micronurse;

import android.content.SharedPreferences;

import com.baidu.mapapi.SDKInitializer;

import org.micronurse.http.MicronurseAPI;
import org.micronurse.util.SharedPreferenceUtil;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    public static String ACTION_SENSOR_DATA_REPORT;
    public static String ACTION_SENSOR_WARNING;
    public static String ACTION_CHAT_MESSAGE_SENT;
    public static String ACTION_CHAT_MESSAGE_RECEIVED;
    public static String ACTION_CHAT_MESSAGE_SEND_START;
    public static String ACTION_MEDICATION_REMINDER;

    public static final String BUNDLE_KEY_USER_ID = "UserId";
    public static final String BUNDLE_KEY_RECEIVER_ID = "ReceiverId";
    public static final String BUNDLE_KEY_TOPIC = "Topic";
    public static final String BUNDLE_KEY_MESSAGE = "Message";
    public static final String BUNDLE_KEY_MESSAGE_ID = "MessageId";
    public static final String BUNDLE_KEY_MESSAGE_TIMESTAMP = "MessageTimestamp";

    @Override
    public void onCreate() {
        super.onCreate();
        //Init Baidu Map SDK
        SDKInitializer.initialize(this);
        //Init Dev Preference
        SharedPreferenceUtil.openDevPreference(this);

        ACTION_SENSOR_DATA_REPORT = getPackageName() + ".action.SENSOR_DATA_REPORT";
        ACTION_SENSOR_WARNING = getPackageName() + ".action.SENSOR_WARNING";
        ACTION_CHAT_MESSAGE_SENT = getPackageName() + ".action.CHAT_MESSAGE_SENT";
        ACTION_CHAT_MESSAGE_RECEIVED = getPackageName() + ".action.CHAT_MESSAGE_RECEIVED";
        ACTION_CHAT_MESSAGE_SEND_START = getPackageName() + ".action.CHAT_MESSAGE_SEND_START";
        ACTION_MEDICATION_REMINDER = getPackageName() + ".action.MEDICATION_REMINDER";
    }
}
