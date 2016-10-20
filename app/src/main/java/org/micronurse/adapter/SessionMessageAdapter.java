package org.micronurse.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.database.model.SessionMessageRecord;
import org.micronurse.util.DateTimeUtil;

import java.util.Date;
import java.util.List;

import cn.nekocode.badge.BadgeDrawable;

/**
 * Created by zhou-shengyun on 16-10-16.
 */

public class SessionMessageAdapter extends RecyclerView.Adapter<SessionMessageAdapter.SessionMessageViewHolder> {
    private Context context;
    private List<MessageItem> sessionList;
    private OnItemClickListener listener;

    public SessionMessageAdapter(Context context, List<MessageItem> sessionList, @Nullable OnItemClickListener listener) {
        this.context = context;
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @Override
    public SessionMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SessionMessageViewHolder holder = new SessionMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_session_message, parent, false));
        holder.portrait = (ImageView) holder.itemView.findViewById(R.id.session_portrait);
        holder.sendingProgress = (ProgressBar) holder.itemView.findViewById(R.id.chat_send_progress);
        holder.sessionTime = (TextView) holder.itemView.findViewById(R.id.session_time);
        holder.sessionMsg = (TextView) holder.itemView.findViewById(R.id.session_msg);
        holder.username = (TextView) holder.itemView.findViewById(R.id.session_displayname);
        holder.badge = (ImageView) holder.itemView.findViewById(R.id.session_badge);
        return holder;
    }

    @Override
    public void onBindViewHolder(SessionMessageViewHolder holder, final int position) {
        final MessageItem messageItem = sessionList.get(position);
        if(messageItem.sending)
            holder.sendingProgress.setVisibility(View.VISIBLE);
        else
            holder.sendingProgress.setVisibility(View.GONE);
        holder.portrait.setImageBitmap(messageItem.portrait);
        holder.username.setText(messageItem.displayName);
        if(messageItem.sessionTime != null)
            holder.sessionTime.setText(DateTimeUtil.convertTimestamp(context, messageItem.sessionTime, true, true));
        if(messageItem.sessionMsg != null && !messageItem.sessionMsg.isEmpty())
            holder.sessionMsg.setText(messageItem.sessionMsg);
        if(messageItem.sessionMessageRecord.getUnreadMessageNum() > 0) {
            BadgeDrawable badgeDrawable = new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(context.getResources().getColor(R.color.red_500))
                    .text1(String.valueOf(messageItem.sessionMessageRecord.getUnreadMessageNum()))
                    .textSize(context.getResources().getDimension(R.dimen.small_text_size))
                    .build();
            holder.badge.setImageDrawable(badgeDrawable);
            holder.badge.setVisibility(View.VISIBLE);
        }else{
            holder.badge.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onItemClick(position, messageItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    class SessionMessageViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private ImageView portrait;
        private TextView username;
        private TextView sessionTime;
        private TextView sessionMsg;
        private ProgressBar sendingProgress;
        private ImageView badge;

        public SessionMessageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position, MessageItem item);
    }

    public static class MessageItem implements Comparable<MessageItem>{
        private Bitmap portrait;
        private Date sessionTime;
        private String sessionMsg;
        private String displayName;
        private SessionMessageRecord sessionMessageRecord;
        private boolean sending = false;

        public MessageItem(Bitmap portrait, String displayName, Date sessionTime, String sessionMsg, SessionMessageRecord sessionMessageRecord, boolean sending) {
            this(portrait, displayName, sessionTime, sessionMsg, sessionMessageRecord);
            this.sending = sending;
        }

        public MessageItem(Bitmap portrait, String displayName, Date sessionTime, String sessionMsg, SessionMessageRecord sessionMessageRecord) {
            this.portrait = portrait;
            this.displayName = displayName;
            this.sessionTime = sessionTime;
            this.sessionMsg = sessionMsg;
            this.sessionMessageRecord = sessionMessageRecord;
        }

        public Bitmap getPortrait() {
            return portrait;
        }

        public void setPortrait(Bitmap portrait) {
            this.portrait = portrait;
        }

        public Date getSessionTime() {
            return sessionTime;
        }

        public void setSessionTime(Date sessionTime) {
            this.sessionTime = sessionTime;
        }

        public String getSessionMsg() {
            return sessionMsg;
        }

        public void setSessionMsg(String sessionMsg) {
            this.sessionMsg = sessionMsg;
        }

        public SessionMessageRecord getSessionMessageRecord() {
            return sessionMessageRecord;
        }

        public boolean isSending() {
            return sending;
        }

        public void setSending(boolean sending) {
            this.sending = sending;
        }

        @Override
        public int compareTo(MessageItem o) {
            if(sessionTime == null && o.sessionTime != null)
                return 1;
            else if(sessionTime != null && o.sessionTime == null)
                return -1;
            else if(sessionTime == null && o.sessionTime == null)
                return 0;
            else if(sessionTime.before(o.sessionTime))
                return 1;
            else
                return -1;
        }
    }
}
