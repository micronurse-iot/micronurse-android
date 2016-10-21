package org.micronurse.ui.listener;

import org.micronurse.database.model.ChatMessageRecord;

import java.util.Date;

/**
 * Created by zhou-shengyun on 16-10-20.
 */

public interface MessageListener {
    void onMessageArrived(ChatMessageRecord cmr);
    void onMessageSent(String receiverId, String messageId);
    void onMessageSendStart(String receiverId, String messageId, String message, Date messageTime);
}
