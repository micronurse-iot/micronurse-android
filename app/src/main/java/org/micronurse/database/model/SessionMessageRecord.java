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
    private String fromUserId;

    @Column(name = "ToUserId", notNull = true, uniqueGroups = {"FromUserId", "ToUserId"},
            indexGroups = {"FromUserId", "ToUserId"})
    private String toUserId;

    @Column(name = "UnreadMessageNum")
    private Integer unreadMessageNum;

    public SessionMessageRecord(){
        super();
    }

    public SessionMessageRecord(String fromUserId, String toUserId, Integer unreadMessageNum) {
        super();
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.unreadMessageNum = unreadMessageNum;
    }

    public SessionMessageRecord(String fromUserId, String toUserId) {
        this(fromUserId, toUserId, 0);
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Integer getUnreadMessageNum() {
        return unreadMessageNum;
    }

    public void setUnreadMessageNum(Integer unreadMessageNum) {
        this.unreadMessageNum = unreadMessageNum;
    }
}
