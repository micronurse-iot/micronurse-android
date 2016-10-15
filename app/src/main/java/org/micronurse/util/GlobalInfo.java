package org.micronurse.util;

import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zhou-shengyun on 7/1/16.
 */
public class GlobalInfo {
    public static String LOG_TAG = "Micronurse";

    public static User user = null;
    public static String token = null;
    public static List<User> guardianshipList = new ArrayList<>();
    public static ConcurrentLinkedQueue<ChatMessageRecord> sendMessageQueue = new ConcurrentLinkedQueue<>();

    public static class Guardian{
        public static User monitorOlder;
    }

    public static void clearLoginUserInfo(){
        user = null;
        token = null;
        guardianshipList.clear();
    }

    public static String TOPIC_SENSOR_DATA_REPORT = "sensor_data_report";
    public static String TOPIC_SENSOR_WARNING = "sensor_warning";
    public static String TOPIC_CHATTING = "chatting";
}
