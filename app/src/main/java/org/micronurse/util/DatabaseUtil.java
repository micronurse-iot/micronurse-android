package org.micronurse.util;

import com.activeandroid.query.Select;

import org.micronurse.database.model.Guardianship;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.model.User;

import java.util.Date;
import java.util.List;

/**
 * Created by zhou-shengyun on 8/28/16.
 */
public class DatabaseUtil {
    public static void updateLoginRecord(User user, String token){
        LoginUserRecord loginRecord = new Select().from(LoginUserRecord.class)
                .where("PhoneNumber=?", user.getPhoneNumber())
                .executeSingle();
        if(loginRecord == null){
            loginRecord = new LoginUserRecord(user.getPhoneNumber(), token, user.getPortrait());
            loginRecord.save();
        }else{
            loginRecord.setLastLoginTime(new Date());
            loginRecord.setPortrait(user.getPortrait());
            loginRecord.setToken(token);
            loginRecord.save();
        }
    }

    public static List<LoginUserRecord> findAllLoginUserRecords(int limit){
        return new Select().from(LoginUserRecord.class)
                .orderBy("LastLoginTime DESC").limit(limit).execute();
    }

    public static LoginUserRecord findLoginUserRecord(String userId){
        return new Select().from(LoginUserRecord.class).where("PhoneNumber=?", userId)
                .executeSingle();
    }

    public static Guardianship findDefaultMonitorOlder(String guardianId){
        return new Select().from(Guardianship.class).where("GuardianID=?", guardianId)
                .executeSingle();
    }
}
