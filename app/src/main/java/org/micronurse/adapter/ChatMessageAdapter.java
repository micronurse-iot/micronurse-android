package org.micronurse.adapter;

import android.content.Context;
import android.graphics.Color;
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

/**
 * Created by zhou-shengyun on 16-10-13.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_LEFT = 1;
    private static final int VIEW_TYPE_MESSAGE_RIGHT = 2;

    private List<Object> messageList;
    private Context context;

    public ChatMessageAdapter(Context context, List<Object> messageList) {
        this.messageList = messageList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = messageList.get(position);
        if(obj instanceof MessageItem){
            switch (((MessageItem) obj).position){
                case MessageItem.POSITION_LEFT:
                    return VIEW_TYPE_MESSAGE_LEFT;
                case MessageItem.POSITION_RIGHT:
                    return VIEW_TYPE_MESSAGE_RIGHT;
            }
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_TYPE_MESSAGE_LEFT:
                return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatbox_left, parent, false));
            case VIEW_TYPE_MESSAGE_RIGHT:
                return new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chatbox_right, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object obj = messageList.get(position);
        if(holder instanceof MessageViewHolder && obj instanceof MessageItem){
            MessageItem messageItem = (MessageItem) obj;
            ((MessageViewHolder) holder).senderPortrait.setImageBitmap(messageItem.sender.getPortrait());
            ((MessageViewHolder) holder).messageTimeText.setText(DateTimeUtil.convertTimestamp(context, messageItem.message.getMessageTime(), true, true));
            if(messageItem.message.getMessageType().equals(ChatMessageRecord.MESSAGE_TYPE_TEXT)){
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
                    if(messageItem.sending)
                        ((MessageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    else
                        ((MessageViewHolder) holder).progressBar.setVisibility(View.GONE);
                }
                chatText.setText(messageItem.message.getContent());
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    private class MessageViewHolder extends RecyclerView.ViewHolder{
        private View itemView;
        private ImageView senderPortrait;
        private ViewGroup chatMessageView;
        private TextView messageTimeText;
        private ProgressBar progressBar;

        public MessageViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            senderPortrait = (ImageView) itemView.findViewById(R.id.chat_sender_portrait);
            chatMessageView = (ViewGroup) itemView.findViewById(R.id.chat_chatbox_view);
            messageTimeText = (TextView) itemView.findViewById(R.id.chat_msg_time);
            progressBar = (ProgressBar) itemView.findViewById(R.id.chat_send_progress);
        }
    }

    public static class MessageItem{
        public static final int POSITION_LEFT = 1;
        public static final int POSITION_RIGHT = 2;

        private int position;
        private User sender;
        private ChatMessageRecord message;
        private boolean sending = false;

        public MessageItem(int position, User sender, ChatMessageRecord message) {
            this.position = position;
            this.sender = sender;
            this.message = message;
        }

        public MessageItem(int position, User sender, ChatMessageRecord message, boolean sending) {
            this(position, sender, message);
            this.sending = sending;
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

        public boolean isSending() {
            return sending;
        }

        public void setSending(boolean sending) {
            this.sending = sending;
        }
    }
}
