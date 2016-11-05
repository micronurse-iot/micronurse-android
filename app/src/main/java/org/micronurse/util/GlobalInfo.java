package org.micronurse.util;

import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.database.model.SessionMessageRecord;
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
    public static String currentChatReceiver = null;

    public static class Guardian{
        public static User monitorOlder;
    }

    public static class Older{
        public static List<User> friendList = new ArrayList<>();
    }

    public static void clearLoginUserInfo(){
        user = null;
        token = null;
        guardianshipList.clear();
        sendMessageQueue.clear();
        currentChatReceiver = null;
        Guardian.monitorOlder = null;
        Older.friendList.clear();
    }

    public static User findUserById(String userId){
        for(User u : guardianshipList){
            if(u.getPhoneNumber().equals(userId))
                return u;
        }
        for(User u : Older.friendList){
            if(u.getPhoneNumber().equals(userId))
                return u;
        }
        return null;
    }

    public static String TOPIC_SENSOR_DATA_REPORT = "sensor_data_report";
    public static String TOPIC_SENSOR_WARNING = "sensor_warning";
    public static String TOPIC_CHATTING = "chatting";
}
