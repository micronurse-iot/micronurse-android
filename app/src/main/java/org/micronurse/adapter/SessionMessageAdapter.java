package org.micronurse.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.database.model.SessionRecord;
import org.micronurse.util.DateTimeUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.nekocode.badge.BadgeDrawable;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-23
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
        return holder;
    }

    @Override
    public void onBindViewHolder(SessionMessageViewHolder holder, final int position) {
        final MessageItem messageItem = sessionList.get(position);
        holder.portrait.setImageBitmap(messageItem.portrait);
        holder.txtDisplayName.setText(messageItem.displayName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onItemClick(position, messageItem);
            }
        });
        ChatMessageRecord cmr = messageItem.session.getLatestMessage();
        if(cmr == null)
            return;
        holder.txtSessionTime.setText(DateTimeUtil.convertTimestamp(context, cmr.getMessageTime(), true, true));
        holder.txtSessionMsg.setText(cmr.getLiteralContent());
        if(cmr.getStatus() == ChatMessageRecord.MESSAGE_STATUS_SENDING)
            holder.sendingProgress.setVisibility(View.VISIBLE);
        else
            holder.sendingProgress.setVisibility(View.GONE);
        if(messageItem.session.getUnreadMessageCount() > 0) {
            BadgeDrawable badgeDrawable = new BadgeDrawable.Builder()
                    .type(BadgeDrawable.TYPE_ONLY_ONE_TEXT)
                    .badgeColor(context.getResources().getColor(R.color.red_500))
                    .text1(String.valueOf(messageItem.session.getUnreadMessageCount()))
                    .textSize(context.getResources().getDimension(R.dimen.small_text_size))
                    .build();
            holder.badge.setImageDrawable(badgeDrawable);
            holder.badge.setVisibility(View.VISIBLE);
        }else{
            holder.badge.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    class SessionMessageViewHolder extends RecyclerView.ViewHolder{
        View itemView;
        @BindView(R.id.session_portrait) ImageView portrait;
        @BindView(R.id.txt_session_display_name) TextView txtDisplayName;
        @BindView(R.id.txt_session_update_time) TextView txtSessionTime;
        @BindView(R.id.txt_session_msg) TextView txtSessionMsg;
        @BindView(R.id.chat_send_progress) ProgressBar sendingProgress;
        @BindView(R.id.session_badge) ImageView badge;

        public SessionMessageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position, MessageItem item);
    }

    public static class MessageItem implements Comparable<MessageItem> {
        private Bitmap portrait;
        private String displayName;
        private SessionRecord session;

        public MessageItem(){}

        public MessageItem(Bitmap portrait, String displayName, SessionRecord session) {
            this.portrait = portrait;
            this.displayName = displayName;
            this.session = session;
        }

        public Bitmap getPortrait() {
            return portrait;
        }

        public void setPortrait(Bitmap portrait) {
            this.portrait = portrait;
        }

        public SessionRecord getSession() {
            return session;
        }

        public void setSession(SessionRecord session) {
            this.session = session;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public int compareTo(MessageItem o) {
            return session.compareTo(o.session);
        }
    }
}
