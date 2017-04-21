package org.micronurse.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhou-shengyun on 16-11-28.
 */

public class SharedPreferenceUtil {
    private static final String PREFERENCE_USER_PREFIX = "user_settings_";
    private static SharedPreferences userPreference;

    public static void openUserPreference(Context context, int userId){
        userPreference = context.getSharedPreferences(PREFERENCE_USER_PREFIX + userId, Context.MODE_PRIVATE);
    }

    public static SharedPreferences getUserPreference() {
        return userPreference;
    }
}
