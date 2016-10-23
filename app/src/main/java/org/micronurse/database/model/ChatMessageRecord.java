package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhou-shengyun on 16-10-13.
 */

@Table(name = "ChatMessageRecord")
public class ChatMessageRecord extends Model implements Serializable {
    public static final String MESSAGE_TYPE_TEXT = "text";
    private static final String MESSAGE_ID_PREFIX = "ChatMessage_";

    @Column(name = "ChatterAId", notNull = true)
    private String chatterAId;

    @Column(name = "ChatterBId", notNull = true)
    private String chatterBId;

    @Column(name = "SenderId", notNull = true)
    private String senderId;

    @Column(name = "MessageTime", notNull = true, uniqueGroups = {"ChatterAId", "ChatterBId", "MessageTime"},
            indexGroups = {"ChatterAId", "ChatterBId", "MessageTime"})
    @Expose
    private Date messageTime;

    @Column(name = "MessageType", notNull = true, length = 10)
    @Expose
    private String messageType;

    @Column(name = "Content", notNull = true)
    @Expose
    private String content;

    public ChatMessageRecord(){
        super();
    }

    public ChatMessageRecord(String chatterAId, String chatterBId, String senderId, String messageType, String content) {
        this(chatterAId, chatterBId, senderId, new Date(), messageType, content);
    }

    public ChatMessageRecord(String chatterAId, String chatterBId, String senderId, Date messageTime, String messageType, String content) {
        this.chatterAId = chatterAId;
        this.chatterBId = chatterBId;
        this.senderId = senderId;
        this.messageTime = messageTime;
        this.messageType = messageType;
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getChatterAId() {
        return chatterAId;
    }

    public void setChatterAId(String chatterAId) {
        this.chatterAId = chatterAId;
    }

    public String getChatterBId() {
        return chatterBId;
    }

    public void setChatterBId(String chatterBId) {
        this.chatterBId = chatterBId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(Date messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageId(){
        return MESSAGE_ID_PREFIX + messageTime.getTime() + '/' + chatterBId + '/' + chatterAId;
    }

    public String getLiteralContent(){
        if(messageType.equals(MESSAGE_TYPE_TEXT))
            return getContent();
        return null;
    }
}
