package org.micronurse.util;

import org.micronurse.model.User;
import java.util.List;

/**
 * Created by zhou-shengyun on 7/1/16.
 */
public class GlobalInfo {
    public static String LOG_TAG = "Micronurse";

    public static User user = null;
    public static String token = null;
    public static List<User> guardianshipList;

    public static class Guardian{
        public static User monitorOlder;
    }

    public static void clearLoginUserInfo(){
        user = null;
        token = null;
        guardianshipList = null;
    }

    public static String TOPIC_SENSOR_DATA_REPORT = "sensor_data_report";
    public static String TOPIC_SENSOR_WARNING = "sensor_warning";

}
