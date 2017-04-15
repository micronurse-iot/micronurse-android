package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
 */

@Table(name = "ChatMessageRecord")
public class ChatMessageRecord extends Model implements Serializable, Comparable<ChatMessageRecord> {
    public static final int MESSAGE_TYPE_TEXT = 1;

    public static final int MESSAGE_STATUS_NORMAL = 0;
    public static final int MESSAGE_STATUS_SENDING = 1;
    public static final int MESSAGE_STATUS_ERROR = 2;

    @Column(name = "Session", notNull = true)
    @Expose
    private SessionRecord session;

    @Column(name = "SenderId", notNull = true)
    @Expose
    private long senderId;

    @Column(name = "MessageTime", notNull = true, uniqueGroups = {"Session", "SenderId", "MessageTime"},
            indexGroups = {"Session", "SenderId", "MessageTime"})
    @Expose
    private Date messageTime;

    @Column(name = "MessageType", notNull = true, length = 10)
    @Expose
    private int messageType;

    @Column(name = "Content", notNull = true)
    @Expose
    private String strContent;

    @Column(name = "Status", notNull = true)
    private int status;

    private Object content;

    public ChatMessageRecord(){
        super();
    }

    public ChatMessageRecord(SessionRecord session, long senderId, int messageType, String strContent) {
        this(session, senderId, new Date(), messageType, strContent, MESSAGE_STATUS_SENDING);
    }

    public ChatMessageRecord(SessionRecord session, long senderId, Date messageTime, int messageType, String strContent, int status) {
        this(session, senderId, messageTime, messageType, strContent, null, status);
    }

    public ChatMessageRecord(SessionRecord session, long senderId, Date messageTime, int messageType, String strContent, Object content, int status) {
        this.session = session;
        this.senderId = senderId;
        this.messageTime = messageTime;
        this.messageType = messageType;
        this.strContent = strContent;
        this.status = status;
        this.content = content;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getStrContent() {
        return strContent;
    }

    public void setStrContent(String strContent) {
        this.strContent = strContent;
    }

    public SessionRecord getSession() {
        return session;
    }

    public void setSession(SessionRecord session) {
        this.session = session;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(Date messageTime) {
        this.messageTime = messageTime;
    }

    public String getLiteralContent(){
        if(messageType == MESSAGE_TYPE_TEXT)
            return getStrContent();
        return null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessageId(){
        return "" + session.getSessionType() + session.getSessionId() + '-'
                + senderId + '-' + messageTime.getTime();
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public int compareTo(ChatMessageRecord o) {
        if(messageTime.before(o.messageTime))
            return 1;
        else if(messageTime.after(o.messageTime))
            return -1;
        return 0;
    }
}
