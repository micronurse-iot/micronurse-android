package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by zhou-shengyun on 16-10-16.
 */

@Table(name = "SessionMessageRecord")
public class SessionMessageRecord extends Model {
    @Column(name = "FromUserId", notNull = true)
    private int fromUserId;

    @Column(name = "ToUserId", notNull = true, uniqueGroups = {"FromUserId", "ToUserId"},
            indexGroups = {"FromUserId", "ToUserId"})
    private int toUserId;

    @Column(name = "UnreadMessageNum")
    private int unreadMessageNum;

    public SessionMessageRecord(){
        super();
    }

    public SessionMessageRecord(int fromUserId, int toUserId, int unreadMessageNum) {
        super();
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.unreadMessageNum = unreadMessageNum;
    }

    public SessionMessageRecord(int fromUserId, int toUserId) {
        this(fromUserId, toUserId, 0);
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public Integer getUnreadMessageNum() {
        return unreadMessageNum;
    }

    public void setUnreadMessageNum(Integer unreadMessageNum) {
        this.unreadMessageNum = unreadMessageNum;
    }
}
