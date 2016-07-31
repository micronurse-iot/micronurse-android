package org.micronurse.util;

import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.model.User;

/**
 * Created by zhou-shengyun on 7/1/16.
 */
public class GlobalInfo {
    public static User user = null;
    public static String token = null;
    public static LoginUserRecord loginRecord = null;

    public static void clearLoginUserInfo(){
        user = null;
        token = null;
        loginRecord = null;
    }
}
