package org.micronurse.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhou-shengyun on 16-11-28.
 */

public class SharedPreferenceUtil {
    public static final String PREFERENCE_DEV = "dev_settings";
    public static final String DEV_KEY_WEBSERVER_HOST = "dev_webserver_host";
    public static final String DEV_KEY_MQTTBROKER_HOST = "dev_mqttbroker_host";

    public static final String PREFERENCE_USER_PREFIX = "user_settings_";

    private static SharedPreferences devPreference;
    private static SharedPreferences userPreference;

    public static void openDevPreference(Context context){
        devPreference = context.getSharedPreferences(PREFERENCE_DEV, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getDevPreference(){
        return devPreference;
    }

    public static void openUserPreference(Context context, String userId){
        userPreference = context.getSharedPreferences(PREFERENCE_USER_PREFIX + userId, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getUserPreference() {
        return userPreference;
    }
}
