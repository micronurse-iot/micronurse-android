package org.micronurse.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

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
        String receiverId = getIntent().getStringExtra(BUNDLE_KEY_RECEIVER_ID);
        for (User u : GlobalInfo.guardianshipList){
            if(u.getPhoneNumber().equals(receiverId))
                chatReceiver = u;
        }
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
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        updateChatHistory();
    }

    private void updateChatHistory(){
        Log.i(GlobalInfo.LOG_TAG, "End time: " + endTime.getTime());
        List<ChatMessageRecord> records = DatabaseUtil.findChatMessageRecords(GlobalInfo.user.getPhoneNumber(),
                chatReceiver.getPhoneNumber(), endTime.getTime(), 20);
        if(records != null){
            for(ChatMessageRecord cmr : records){
                endTime.setTimeInMillis(cmr.getMessageTime().getTime() - 1);
                if(GlobalInfo.user.getPhoneNumber().equals(cmr.getSenderId())){
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
        ChatMessageRecord newMessage = new ChatMessageRecord(GlobalInfo.user.getPhoneNumber(), chatReceiver.getPhoneNumber(),
                GlobalInfo.user.getPhoneNumber(), ChatMessageRecord.MESSAGE_TYPE_TEXT, message);
        GlobalInfo.sendMessageQueue.add(newMessage);
        messageList.addLast(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_RIGHT, GlobalInfo.user, newMessage));
        adapter.notifyItemInserted(messageList.size() - 1);
        chatListView.smoothScrollToPosition(messageList.size() - 1);
        mqttService.addMQTTAction(new MQTTService.MQTTPublishAction(
                GlobalInfo.TOPIC_CHATTING, GlobalInfo.user.getPhoneNumber(), chatReceiver.getPhoneNumber(),
                1, GsonUtil.getDefaultGsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(newMessage),
                "Hello", Application.ACTION_CHAT_MESSAGE_SENT
        ));
        editChatMsg.setText("");
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
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
