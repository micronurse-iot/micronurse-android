package org.micronurse.database.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by zhou-shengyun on 16-10-13.
 */

@Table(name = "ChatMessageRecord")
public class ChatMessageRecord extends Model {
    public static final String MESSAGE_TYPE_TEXT = "text";

    @Column(name = "SenderId", notNull = true)
    private String senderId;

    @Column(name = "ReceiverId", notNull = true)
    private String receiverId;

    @Column(name = "MessageTime", notNull = true, uniqueGroups = {"SenderId", "ReceiverId", "MessageTime"},
            indexGroups = {"SenderId", "ReceiverId", "MessageTime"})
    private Date messageTime;

    @Column(name = "MessageType", notNull = true, length = 10)
    private String messageType;

    @Column(name = "Content", notNull = true)
    private String content;

    public ChatMessageRecord(){
        super();
    }

    public ChatMessageRecord(String senderId, String receiverId, Date messageTime, String messageType, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Date getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(Date messageTime) {
        this.messageTime = messageTime;
    }
}
