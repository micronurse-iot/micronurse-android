package org.micronurse.util;

import android.content.Context;
import android.content.Intent;

import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.model.User;
import org.micronurse.service.EmergencyCallService;
import org.micronurse.service.LocationService;
import org.micronurse.service.MQTTService;

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
    public static Integer currentChatReceiverId = null;

    public static class Guardian{
        public static User monitorOlder;
    }

    public static class Older{
        public static List<User> friendList = new ArrayList<>();
    }

    private static void clearLoginUserInfo(){
        user = null;
        token = null;
        guardianshipList.clear();
        sendMessageQueue.clear();
        currentChatReceiverId = null;
        Guardian.monitorOlder = null;
        Older.friendList.clear();
    }

    public static void exitLoginStatus(Context context){
        Intent intent = new Intent();
        if(user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            intent.setClass(context, LocationService.class);
            context.stopService(intent);
            intent.setClass(context, EmergencyCallService.class);
            context.stopService(intent);
        }

        intent.setClass(context, MQTTService.class);
        context.stopService(intent);
        clearLoginUserInfo();
    }

    public static User findUserById(int userId){
        if(user.getUserId() == userId)
            return user;
        for(User u : guardianshipList){
            if(u.getUserId() == userId)
                return u;
        }
        for(User u : Older.friendList){
            if(u.getUserId() == userId)
                return u;
        }
        return null;
    }

    public static String TOPIC_SENSOR_DATA_REPORT = "sensor_data_report";
    public static String TOPIC_SENSOR_WARNING = "sensor_warning";
    public static String TOPIC_CHATTING = "chatting";
}
