package org.micronurse.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.micronurse.Application;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.util.GlobalInfo;

public class ChatMessageSentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ChatMessageRecord cmr = GlobalInfo.sendMessageQueue.poll();
        if(cmr != null){
            cmr.save();
            intent.setAction(Application.ACTION_CHAT_MESSAGE_SENT_CACHED);
            context.sendBroadcast(intent);
            Log.i(GlobalInfo.LOG_TAG, "Cache a message to user " + cmr.getChatterBId() + " successfully.");
        }
    }
}
