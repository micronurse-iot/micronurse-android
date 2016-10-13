package org.micronurse.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import org.micronurse.R;
import org.micronurse.adapter.ChatMessageAdapter;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.model.User;
import org.micronurse.util.GlobalInfo;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    public static final String BUNDLE_KEY_RECEIVER_ID = "ReceiverId";

    private RecyclerView chatListView;
    private User receiver;
    private List<Object> messageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        chatListView = (RecyclerView) findViewById(R.id.chat_list);
        chatListView.setLayoutManager(new LinearLayoutManager(this));
        String receiverId = getIntent().getStringExtra(BUNDLE_KEY_RECEIVER_ID);
        for (User u : GlobalInfo.guardianshipList){
            if(u.getPhoneNumber().equals(receiverId))
                receiver = u;
        }
        setTitle(receiver.getNickname());
        messageList.add(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_LEFT,
                receiver, new ChatMessageRecord(null, null, null, ChatMessageRecord.MESSAGE_TYPE_TEXT, "Hello, world!")));
        messageList.add(new ChatMessageAdapter.MessageItem(ChatMessageAdapter.MessageItem.POSITION_RIGHT,
                GlobalInfo.user, new ChatMessageRecord(null, null, null, ChatMessageRecord.MESSAGE_TYPE_TEXT, "Hello, world, too!")));
        chatListView.setAdapter(new ChatMessageAdapter(this, messageList));
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
}
