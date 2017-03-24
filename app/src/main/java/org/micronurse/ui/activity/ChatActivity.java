package org.micronurse.ui.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.gson.JsonSyntaxException;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.adapter.ChatMessageAdapter;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_RECEIVER_ID = "ReceiverId";

    private SwipeRefreshLayout refresh;
    private RecyclerView chatListView;
    private LinearLayoutManager layoutManager;
    private ImageButton btnSendMessage;
    private EditText editChatMsg;
    private User chatReceiver;
    private LinkedList<Object> messageList = new LinkedList<>();
    private ChatMessageAdapter adapter;
    private Calendar endTime;
    private ServiceConnection serviceConnection;
    private MQTTService mqttService;
    private ChatMessageSentReceiver msgSentReceiver;
    private ChatMessageArrivedReceiver msgArrivedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endTime = Calendar.getInstance();

        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateChatHistory();
            }
        });
        chatListView = (RecyclerView) findViewById(R.id.chat_list);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        chatListView.setLayoutManager(layoutManager);
        chatListView.setNestedScrollingEnabled(false);
        int receiverId = getIntent().getIntExtra(BUNDLE_KEY_RECEIVER_ID, -1);
        chatReceiver = GlobalInfo.findUserById(receiverId);
        setTitle(chatReceiver.getNickname());
        adapter = new ChatMessageAdapter(this, messageList);
        chatListView.setAdapter(adapter);
        btnSendMessage = (ImageButton) findViewById(R.id.btn_send_msg);
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });
        editChatMsg = (EditText) findViewById(R.id.chat_msg_edit);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mqttService = ((MQTTService.MQTTServiceBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };

        msgSentReceiver = new ChatMessageSentReceiver();
        IntentFilter intentFilter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SENT);
        intentFilter.addCategory(getPackageName());
        registerReceiver(msgSentReceiver, intentFilter);
        msgArrivedReceiver = new ChatMessageArrivedReceiver();
        intentFilter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED);
        intentFilter.addCategory(getPackageName());
        registerReceiver(msgArrivedReceiver, intentFilter);
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        updateChatHistory();
        for(ChatMessageRecord cmr : GlobalInfo.sendMessageQueue){
            if(cmr.getChatterAId() == GlobalInfo.user.getUserId() && cmr.getChatterBId() == receiverId){
                messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_RIGHT, GlobalInfo.user, cmr, true));
            }
        }
        Collections.sort(messageList, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                if(((ChatMessageAdapter.MessageItem) o1).getMessage().getMessageTime().getTime() <
                        ((ChatMessageAdapter.MessageItem) o2).getMessage().getMessageTime().getTime())
                    return -1;
                return 1;
            }
        });
        if(!messageList.isEmpty()) {
            adapter.notifyDataSetChanged();
            chatListView.smoothScrollToPosition(messageList.size() - 1);
        }
        chatListView.setVisibility(View.VISIBLE);
    }

    private void updateChatHistory(){
        List<ChatMessageRecord> records = DatabaseUtil.findChatMessageRecords(GlobalInfo.user.getUserId(),
                chatReceiver.getUserId(), endTime.getTime(), 20);
        if(records != null){
            for(ChatMessageRecord cmr : records){
                endTime.setTimeInMillis(cmr.getMessageTime().getTime() - 1);
                if(GlobalInfo.user.getUserId() == cmr.getSenderId()){
                    messageList.addFirst(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_RIGHT,
                            GlobalInfo.user, cmr));
                }else{
                    messageList.addFirst(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_LEFT,
                            chatReceiver, cmr));
                }
                adapter.notifyItemInserted(0);
            }
            if(records.size() > 0)
                chatListView.smoothScrollToPosition(records.size() - 1);
        }
        refresh.setRefreshing(false);
    }

    private void sendTextMessage(){
        String message = editChatMsg.getText().toString();
        if(message.isEmpty())
            return;
        ChatMessageRecord newMessage = new ChatMessageRecord(GlobalInfo.user.getUserId(), chatReceiver.getUserId(),
                GlobalInfo.user.getUserId(), ChatMessageRecord.MESSAGE_TYPE_TEXT, message);
        sendMessage(newMessage);
        editChatMsg.setText("");
    }

    private void sendMessage(ChatMessageRecord newMessage){
        GlobalInfo.sendMessageQueue.add(newMessage);
        messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_RIGHT, GlobalInfo.user, newMessage, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        chatListView.smoothScrollToPosition(messageList.size() - 1);

        Intent intent = new Intent(Application.ACTION_CHAT_MESSAGE_SEND_START);
        intent.addCategory(getPackageName());
        intent.putExtra(Application.BUNDLE_KEY_MESSAGE, newMessage.getLiteralContent());
        intent.putExtra(Application.BUNDLE_KEY_MESSAGE_ID, newMessage.getMessageId());
        intent.putExtra(Application.BUNDLE_KEY_RECEIVER_ID, chatReceiver.getUserId());
        intent.putExtra(Application.BUNDLE_KEY_MESSAGE_TIMESTAMP, newMessage.getMessageTime().getTime());
        sendBroadcast(intent);

        mqttService.addMQTTAction(new MQTTService.MQTTPublishAction(
                GlobalInfo.TOPIC_CHATTING, GlobalInfo.user.getUserId(), chatReceiver.getUserId(),
                1, GsonUtil.getDefaultGsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(newMessage),
                newMessage.getMessageId(), Application.ACTION_CHAT_MESSAGE_SENT
        ));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(msgArrivedReceiver);
        unregisterReceiver(msgSentReceiver);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private class ChatMessageSentReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int topicUserId = intent.getIntExtra(Application.BUNDLE_KEY_USER_ID, -1);
            int receiverId = intent.getIntExtra(Application.BUNDLE_KEY_RECEIVER_ID, -1);
            if(GlobalInfo.user == null || GlobalInfo.user.getUserId() != topicUserId || chatReceiver.getUserId() != receiverId)
                return;
            String messageId = intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE_ID);
            if(messageId == null || messageId.isEmpty())
                return;
            for(int i = messageList.size() - 1; i >= 0; i--){
                Object item = messageList.get(i);
                if(item instanceof ChatMessageAdapter.MessageItem){
                    if(messageId.equals(((ChatMessageAdapter.MessageItem) item).getMessage().getMessageId())){
                        ((ChatMessageAdapter.MessageItem) item).setSending(false);
                        adapter.notifyItemChanged(i);
                        return;
                    }
                }
            }
        }
    }

    private class ChatMessageArrivedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(GlobalInfo.user == null || GlobalInfo.user.getUserId() != intent.getIntExtra(Application.BUNDLE_KEY_RECEIVER_ID, -1))
                return;
            int senderId = intent.getIntExtra(Application.BUNDLE_KEY_USER_ID, -1);
            if(chatReceiver.getUserId() != senderId)
                return;
            try {
                ChatMessageRecord cmr = GsonUtil.getDefaultGsonBuilder()
                        .excludeFieldsWithoutExposeAnnotation().create()
                        .fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE), ChatMessageRecord.class);
                cmr.setChatterAId(GlobalInfo.user.getUserId());
                cmr.setChatterBId(senderId);
                cmr.setSenderId(senderId);
                messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_LEFT, chatReceiver, cmr));
                boolean scrollFlag = !chatListView.canScrollVertically(1);
                adapter.notifyItemInserted(messageList.size() - 1);
                if(scrollFlag)
                    chatListView.smoothScrollToPosition(messageList.size() - 1);
            }catch (JsonSyntaxException jse){
                jse.printStackTrace();
            }
        }
    }
}
