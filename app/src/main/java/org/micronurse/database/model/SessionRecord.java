package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */

@Table(name = "SessionRecord")
public class SessionRecord extends Model implements Comparable<SessionRecord>, Serializable {
    public static final char SESSION_TYPE_UNKNOWN = 'U';
    public static final char SESSION_TYPE_GROUP = 'A';
    public static final char SESSION_TYPE_GUARDIANSHIP = 'B';
    public static final char SESSION_TYPE_FRIEND = 'C';

    @Column(name = "OwnerUserId", notNull = true)
    private long ownerUserId;

    @Column(name = "SessionId", notNull = true, uniqueGroups = {"OwnerUserId", "SessionId"},
            indexGroups = {"OwnerUserId", "SessionId"})
    @Expose
    private String originalSessionId;

    @Column(name = "UnreadMessageCount")
    private int unreadMessageCount;

    public SessionRecord(){
        super();
    }

    public SessionRecord(long ownerUserId, char sessionType, long sessionId, int unreadMessageCount) {
        super();
        this.ownerUserId = ownerUserId;
        this.originalSessionId = getOriginalSessionId(sessionType, sessionId);
        this.unreadMessageCount = unreadMessageCount;
    }

    public SessionRecord(long ownerUserId, char sessionType, long sessionId) {
        this(ownerUserId, sessionType, sessionId, 0);
    }

    public long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public char getSessionType(){
        if(originalSessionId == null || originalSessionId.isEmpty())
            return SESSION_TYPE_UNKNOWN;
        else
            return originalSessionId.charAt(0);
    }

    public long getSessionId(){                 //Session ID represents group ID or user ID
        if(originalSessionId == null)
            return -1;
        return Long.valueOf(originalSessionId.substring(1));
    }

    public Integer getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(Integer unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public List<ChatMessageRecord> getAllMessages(){
        return getMany(ChatMessageRecord.class, "Session");
    }

    public ChatMessageRecord getLatestMessage(){
        return new Select().from(ChatMessageRecord.class).where("Session=?", getId())
                .orderBy("MessageTime DESC").limit(1).executeSingle();
    }

    public List<ChatMessageRecord> getMessages(Date endTime, int limit){
        return new Select().from(ChatMessageRecord.class).where("Session=?", getId())
                .where("MessageTime<=?", endTime.getTime())
                .orderBy("MessageTime DESC").limit(limit).execute();
    }

    @Override
    public int compareTo(SessionRecord o) {
        ChatMessageRecord thisMsg = getLatestMessage();
        ChatMessageRecord thatMsg = o.getLatestMessage();
        if(thisMsg == null && thatMsg == null)
            return 0;
        else if(thisMsg != null && thatMsg == null)
            return -1;
        else if(thisMsg == null)
            return 1;
        return getLatestMessage().compareTo(o.getLatestMessage());
    }

    public static String getOriginalSessionId(char sessionType, long sessionId){
        return "" + sessionType + sessionId;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj instanceof SessionRecord){
            if(originalSessionId.equals(((SessionRecord) obj).originalSessionId) &&
                    ownerUserId == ((SessionRecord) obj).ownerUserId)
                return true;
        }
        return false;
    }
}
