package org.micronurse.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.micronurse.Application;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

public class ChatMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Application.ACTION_CHAT_MESSAGE_SENT)) {
            if(GlobalInfo.sendMessageQueue.isEmpty())
                return;
            ChatMessageRecord cmr = GlobalInfo.sendMessageQueue.poll();
            cmr.save();
            Log.i(GlobalInfo.LOG_TAG, "Cache a message to user " + cmr.getChatterBId() + " successfully.");
        } else if (intent.getAction().equals(Application.ACTION_CHAT_MESSAGE_RECEIVED)) {
            if(!GlobalInfo.user.getPhoneNumber().equals(intent.getStringExtra(Application.BUNDLE_KEY_RECEIVER_ID)))
                return;
            String senderId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            ChatMessageRecord cmr = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE),
                    ChatMessageRecord.class);
            cmr = new ChatMessageRecord(GlobalInfo.user.getPhoneNumber(), senderId, senderId, cmr.getMessageTime(), cmr.getMessageType(), cmr.getContent());
            cmr.save();
            Log.i(GlobalInfo.LOG_TAG, "Cache a message from user " + senderId + " successfully.");
        }
    }
}
