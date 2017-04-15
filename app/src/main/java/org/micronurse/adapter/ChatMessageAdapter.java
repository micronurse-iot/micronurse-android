package org.micronurse.adapter;

import android.content.Context;
import android.graphics.Color;
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
import org.micronurse.model.User;
import org.micronurse.util.DateTimeUtil;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-02-28
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_CHAT_MESSAGE_LEFT = 1;
    private static final int VIEW_TYPE_CHAT_MESSAGE_RIGHT = 2;

    private List<Object> messageList;
    private Context context;
    private boolean showSenderName = false;

    public ChatMessageAdapter(Context context, List<Object> messageList) {
        this(context, messageList, false);
    }

    public ChatMessageAdapter(Context context, List<Object> messageList, boolean showSenderName) {
        this.messageList = messageList;
        this.context = context;
        this.showSenderName = showSenderName;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = messageList.get(position);
        if(obj instanceof MessageItem){
            switch (((MessageItem) obj).position){
                case MessageItem.POSITION_LEFT:
                    return VIEW_TYPE_CHAT_MESSAGE_LEFT;
                case MessageItem.POSITION_RIGHT:
                    return VIEW_TYPE_CHAT_MESSAGE_RIGHT;
            }
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_CHAT_MESSAGE_LEFT:
                return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatbox_left, parent, false));
            case VIEW_TYPE_CHAT_MESSAGE_RIGHT:
                return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatbox_right, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = messageList.get(position);
        if(holder instanceof MessageViewHolder && obj instanceof MessageItem){
            final MessageItem messageItem = (MessageItem) obj;
            ((MessageViewHolder) holder).senderPortrait.setImageBitmap(messageItem.sender.getPortrait());
            ((MessageViewHolder) holder).senderPortrait.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(messageItem.listener != null)
                        messageItem.listener.onClick(messageItem.sender);
                }
            });
            ((MessageViewHolder) holder).messageTimeText.setText(DateTimeUtil.convertTimestamp(context, messageItem.message.getMessageTime(), true, true));
            if(messageItem.message.getMessageType() == ChatMessageRecord.MESSAGE_TYPE_TEXT){
                TextView chatText = (TextView) ((MessageViewHolder) holder).chatMessageView.findViewWithTag("text");
                if(chatText == null) {
                    chatText = new TextView(context);
                    chatText.setTag("text");
                    chatText.setTextIsSelectable(true);
                    if(messageItem.position == MessageItem.POSITION_LEFT)
                        chatText.setTextColor(Color.BLACK);
                    else
                        chatText.setTextColor(Color.WHITE);
                    ((MessageViewHolder) holder).chatMessageView.addView(chatText);
                }
                if(((MessageViewHolder) holder).progressBar != null){
                    if(messageItem.getMessage().getStatus() == ChatMessageRecord.MESSAGE_STATUS_SENDING)
                        ((MessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    else
                        ((MessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                }
                if(((MessageViewHolder) holder).senderNameText != null){
                    if(!showSenderName)
                        ((MessageViewHolder) holder).senderNameText.setVisibility(View.GONE);
                    else {
                        ((MessageViewHolder) holder).senderNameText.setVisibility(View.VISIBLE);
                        ((MessageViewHolder) holder).senderNameText.setText(messageItem.sender.getNickname());
                    }
                }
                chatText.setText(messageItem.message.getStrContent());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.chat_sender_portrait) ImageView senderPortrait;
        @BindView(R.id.chat_chatbox_view) ViewGroup chatMessageView;
        @BindView(R.id.chat_msg_time) TextView messageTimeText;
        @Nullable @BindView(R.id.chat_sender_name) TextView senderNameText;
        @Nullable @BindView(R.id.chat_send_progress) ProgressBar progressBar;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class MessageItem{
        public static final int POSITION_LEFT = 1;
        public static final int POSITION_RIGHT = 2;

        private int position;
        private User sender;
        private ChatMessageRecord message;
        private OnSenderClickListener listener;

        public MessageItem(int position, User sender, ChatMessageRecord message, @Nullable OnSenderClickListener listener) {
            this.position = position;
            this.sender = sender;
            this.message = message;
            this.listener = listener;
        }

        public ChatMessageRecord getMessage() {
            return message;
        }

        public User getSender() {
            return sender;
        }

        public int getPosition() {
            return position;
        }

        public interface OnSenderClickListener {
            void onClick(User sender);
        }
    }
}
