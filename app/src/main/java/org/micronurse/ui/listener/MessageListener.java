package org.micronurse.ui.listener;

import org.micronurse.database.model.ChatMessageRecord;

import java.util.Date;

/**
 * Created by zhou-shengyun on 16-10-20.
 */

public interface MessageListener {
    void onMessageArrived(ChatMessageRecord cmr);
    void onMessageSent(ChatMessageRecord cmr);
    void onMessageSendStart(ChatMessageRecord cmr);
}
