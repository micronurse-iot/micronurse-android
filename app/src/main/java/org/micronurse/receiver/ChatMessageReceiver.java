package org.micronurse.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.NotificationCompat;

import com.google.gson.Gson;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.database.model.SessionRecord;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-03-03
 */
public class ChatMessageReceiver extends BroadcastReceiver {
    public static final int CHAT_MSG_NOTIFICATION_ID = 0x28738;
    private Gson gson = GsonUtil.getDefaultGsonBuilder()
            .excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public void onReceive(Context context, Intent mqttIntent) {
        if(mqttIntent.getAction().equals(Application.ACTION_CHAT_MESSAGE_RECEIVED)) {
            ChatMessageRecord cmr = gson.fromJson(mqttIntent.getStringExtra(MQTTService.BUNDLE_KEY_MESSAGE),
                    ChatMessageRecord.class);
            if (!checkReceivedChatMessage(mqttIntent, cmr))
                return;

            long sessionId = -1;
            char sessionType = cmr.getSession().getSessionType();
            switch (sessionType){
                case SessionRecord.SESSION_TYPE_FRIEND:
                case SessionRecord.SESSION_TYPE_GUARDIANSHIP:
                    sessionId = cmr.getSenderId();
                    break;
                case SessionRecord.SESSION_TYPE_GROUP:
                    sessionId = cmr.getSession().getSessionId();
                    break;
            }

            SessionRecord session = DatabaseUtil.findSessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId);
            if (session == null)
                session = new SessionRecord(GlobalInfo.user.getUserId(), sessionType, sessionId, 0);
            session.setUnreadMessageCount(session.getUnreadMessageCount() + 1);
            ChatMessageRecord newMessage = new ChatMessageRecord(session, cmr.getSenderId(), cmr.getMessageTime(), cmr.getMessageType(),
                    cmr.getStrContent(), ChatMessageRecord.MESSAGE_STATUS_NORMAL);
            Intent intent = new Intent(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED);
            intent.addCategory(context.getPackageName());
            try {
                session.save();
                intent.putExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, newMessage.save());
                context.sendBroadcast(intent);
                showMsgNotification(context, newMessage);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else if(mqttIntent.getAction().equals(Application.ACTION_CHAT_MESSAGE_SENT)){
            ChatMessageRecord cmr = DatabaseUtil.findChatMessageByDbId(mqttIntent.getLongExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, -1));
            if(cmr != null){
                cmr.setStatus(ChatMessageRecord.MESSAGE_STATUS_NORMAL);
                cmr.save();
                Intent intent = new Intent(Application.ACTION_CHAT_MESSAGE_SENT_SAVED);
                intent.addCategory(context.getPackageName());
                intent.putExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, cmr.getId());
                context.sendBroadcast(intent);
            }
        }
    }

    private void showMsgNotification(Context context, ChatMessageRecord newMessage){
        if(GlobalInfo.currentSession != null && newMessage.getSession().equals(GlobalInfo.currentSession))
            return;
        Bitmap portrait = null;
        String displayName = null;
        String groupSenderName = null;
        String content;
        User u = null;
        int iconRes = R.drawable.ic_chat;
        switch (newMessage.getSession().getSessionType()){
            case SessionRecord.SESSION_TYPE_GUARDIANSHIP:
            case SessionRecord.SESSION_TYPE_FRIEND:
                u = GlobalInfo.findUserById(newMessage.getSenderId());
                if(u == null)
                    return;
                portrait = u.getPortrait();
                displayName = u.getNickname();
                content = newMessage.getLiteralContent();
                break;
            default:
                return;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(iconRes)
                .setAutoCancel(true)
                .setContentTitle(displayName)
                .setContentText(content)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if(portrait != null)
           mBuilder.setLargeIcon(portrait);

        Intent msgIntent = new Intent(context, ChatActivity.class);
        msgIntent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, newMessage.getSession().getSessionType());
        msgIntent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, newMessage.getSession().getSessionId());
        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, msgIntent, 0));

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(CHAT_MSG_NOTIFICATION_ID, mBuilder.build());
        GlobalInfo.notificationSession = newMessage.getSession();
    }

    private static boolean checkReceivedChatMessage(Intent mqttIntent, ChatMessageRecord cmr){
        if(GlobalInfo.user == null)
            return false;
        if(cmr.getSenderId() == GlobalInfo.user.getUserId())
            return false;
        switch (cmr.getSession().getSessionType()){
            case SessionRecord.SESSION_TYPE_GUARDIANSHIP:
            case SessionRecord.SESSION_TYPE_FRIEND:
                if(GlobalInfo.findUserById(cmr.getSenderId()) == null)
                    return false;
                if(mqttIntent.getLongExtra(MQTTService.BUNDLE_KEY_TOPIC_OWNER_ID, -1) != GlobalInfo.user.getUserId())
                    return false;
                break;
        }
        return true;
    }
}
