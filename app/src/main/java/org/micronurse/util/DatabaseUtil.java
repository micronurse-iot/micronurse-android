package org.micronurse.util;

import com.activeandroid.query.Select;

import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.database.model.MedicationReminder;
import org.micronurse.database.model.SessionRecord;
import org.micronurse.model.User;

import java.util.Date;
import java.util.List;

/**
 * Created by zhou-shengyun on 8/28/16.
 */
public class DatabaseUtil {
    public static void updateLoginRecord(User user, String token){
        LoginUserRecord loginRecord = new Select().from(LoginUserRecord.class)
                .where("UserId=?", user.getUserId())
                .executeSingle();
        if(loginRecord == null){
            loginRecord = new LoginUserRecord(user.getUserId(), user.getPhoneNumber(), token, user.getPortrait());
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

    public static LoginUserRecord findLoginUserRecord(long userId){
        return new Select().from(LoginUserRecord.class).where("UserId=?", userId)
                .executeSingle();
    }

    public static List<ChatMessageRecord> findChatMessageRecords(int chatterAId, int chatterBId, Date endTime, int limit){
        return new Select().from(ChatMessageRecord.class)
                .where("ChatterAId=?", chatterAId)
                .where("ChatterBId=?", chatterBId)
                .where("MessageTime<=?", endTime.getTime())
                .orderBy("MessageTime DESC")
                .limit(limit)
                .execute();
    }

    public static List<SessionRecord> findAllSessionRecords(long ownerUserId){
        return new Select().from(SessionRecord.class).where("OwnerUserId=?", ownerUserId)
                .execute();
    }

    public static SessionRecord findSessionRecord(long ownerUserId, char sessionType, long sessionId){
        return new Select().from(SessionRecord.class).where("OwnerUserId=?", ownerUserId)
                .where("SessionId=?", SessionRecord.getOriginalSessionId(sessionType, sessionId))
                .executeSingle();
    }

    public static SessionRecord findSessionRecordByDbId(long dbId){
        return new Select().from(SessionRecord.class).where("Id=?", dbId)
                .executeSingle();
    }

    public static ChatMessageRecord findChatMessageByDbId(long dbId){
        return new Select().from(ChatMessageRecord.class).where("Id=?", dbId)
                .executeSingle();
    }

    public static List<MedicationReminder> findMedicationRemindersByUserId(long userId){
        return new Select().from(MedicationReminder.class)
                .where("UserId=?", userId)
                .orderBy("AddTime DESC")
                .execute();
    }

    public static MedicationReminder findMedicationReminderByDBId(long id){
        return new Select().from(MedicationReminder.class)
                .where("Id=?", id)
                .executeSingle();
    }
}
