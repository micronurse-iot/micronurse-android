package org.micronurse;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    public static String ACTION_SENSOR_DATA_REPORT;
    public static String ACTION_SENSOR_WARNING;
    public static String ACTION_CHAT_MESSAGE_SENT;
    public static String ACTION_CHAT_MESSAGE_SENT_CACHED;

    public static final String BUNDLE_KEY_USER_ID = "UserId";
    public static final String BUNDLE_KEY_RECEIVER_ID = "ReceiverId";
    public static final String BUNDLE_KEY_TOPIC = "Topic";
    public static final String BUNDLE_KEY_MESSAGE = "Message";
    public static final String BUNDLE_KEY_MESSAGE_ID = "MessageId";

    @Override
    public void onCreate() {
        super.onCreate();
        //Init Baidu Map SDK
        SDKInitializer.initialize(this);
        ACTION_SENSOR_DATA_REPORT = getPackageName() + ".action.SENSOR_DATA_REPORT";
        ACTION_SENSOR_WARNING = getPackageName() + ".action.SENSOR_WARNING";
        ACTION_CHAT_MESSAGE_SENT = getPackageName() + ".action.CHAT_MESSAGE_SENT";
        ACTION_CHAT_MESSAGE_SENT_CACHED = getPackageName() + ".action.CHAT_MESSAGE_SENT_CACHED";
    }
}
