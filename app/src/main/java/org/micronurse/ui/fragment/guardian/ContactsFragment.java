package org.micronurse.ui.fragment.guardian;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.adapter.SessionMessageAdapter;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.database.model.SessionMessageRecord;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class ContactsFragment extends Fragment implements OnBindMQTTServiceListener {
    public static int REQUEST_CODE_CHAT_ACTIVITY = 100;

    private View viewRoot;
    private RecyclerView sessionListView;
    private FloatingActionButton btnAddContact;
    private LinkedList<SessionMessageAdapter.MessageItem> contactsSessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;
    private MessageArrivedReceiver msgArrivedReceiver;
    private MessageSentReceiver msgSentReceiver;

    private String ignoreSenderId = null;

    public ContactsFragment() {
        // Required empty public constructor
        msgArrivedReceiver = new MessageArrivedReceiver();
        msgSentReceiver = new MessageSentReceiver();
    }

    public static ContactsFragment getInstance(Context context){
        ContactsFragment fragment = new ContactsFragment();
        IntentFilter filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED);
        filter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.msgArrivedReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SENT);
        filter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.msgSentReceiver, filter);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_guardian_contacts, container, false);
        sessionListView = (RecyclerView) viewRoot.findViewById(R.id.session_msg_list);
        sessionListView.setNestedScrollingEnabled(false);
        sessionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sessionListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && btnAddContact.isShown())
                    btnAddContact.hide();
                else if(dy < 0 && !btnAddContact.isShown())
                    btnAddContact.show();
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        btnAddContact = (FloatingActionButton) viewRoot.findViewById(R.id.btn_add_older);
        if(GlobalInfo.guardianshipList.isEmpty())
            return viewRoot;
        viewRoot.findViewById(R.id.txt_no_contact).setVisibility(View.GONE);
        List<SessionMessageRecord> records = DatabaseUtil.findSessionMessageRecords(GlobalInfo.user.getPhoneNumber());
        for(SessionMessageRecord smr : records){
            for(User u : GlobalInfo.guardianshipList){
                if(u.getPhoneNumber().equals(smr.getToUserId())){
                    List<ChatMessageRecord> chatRecords = DatabaseUtil.findChatMessageRecords(GlobalInfo.user.getPhoneNumber(),
                            smr.getToUserId(), new Date(), 1);
                    if(chatRecords != null && !chatRecords.isEmpty()) {
                        if (chatRecords.get(0).getMessageType().equals(ChatMessageRecord.MESSAGE_TYPE_TEXT)) {
                            contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(),
                                    chatRecords.get(0).getMessageTime(), chatRecords.get(0).getContent(), smr));
                            break;
                        }
                    }
                }
                contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(),
                        null, null, smr));
            }
        }
        for(User u : GlobalInfo.guardianshipList) {
            boolean findFlag = false;
            for (SessionMessageAdapter.MessageItem mi : contactsSessionList) {
                if (u.getPhoneNumber().equals(mi.getSessionMessageRecord().getToUserId())) {
                    findFlag = true;
                    break;
                }
            }
            if(!findFlag){
                SessionMessageRecord smr = new SessionMessageRecord(GlobalInfo.user.getPhoneNumber(), u.getPhoneNumber());
                smr.save();
                contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(),
                        null, null, smr));
            }
        }
        Collections.sort(contactsSessionList);
        adapter = new SessionMessageAdapter(getContext(), contactsSessionList, new SessionMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SessionMessageAdapter.MessageItem item) {
                item.getSessionMessageRecord().setUnreadMessageNum(0);
                item.getSessionMessageRecord().save();
                adapter.notifyItemChanged(position);
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(ChatActivity.BUNDLE_KEY_RECEIVER_ID, item.getSessionMessageRecord().getToUserId());
                ignoreSenderId = item.getSessionMessageRecord().getToUserId();
                startActivityForResult(intent, REQUEST_CODE_CHAT_ACTIVITY);
            }
        });
        sessionListView.setAdapter(adapter);

        return viewRoot;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_CHAT_ACTIVITY){
            Log.i(GlobalInfo.LOG_TAG, "onActivityResult:" + resultCode);
            if(data != null){
                String message = data.getStringExtra(ChatActivity.BUNDLE_KEY_LAST_TEXT_MESSAGE);
                if(message != null && !message.isEmpty()){
                    Date messageTime = new Date(data.getLongExtra(ChatActivity.BUNDLE_KEY_LAST_MESSAGE_TIMESTAMP, -1));
                    int pos = 0;
                    Log.i(GlobalInfo.LOG_TAG, "onActivityResult with data.");
                    for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
                        if(mi.getSessionMessageRecord().getToUserId().equals(ignoreSenderId)){
                            mi.getSessionMessageRecord().setUnreadMessageNum(0);
                            mi.getSessionMessageRecord().save();
                            mi.setSending(data.getBooleanExtra(ChatActivity.BUNDLE_KEY_SENDING, false));
                            mi.setSessionTime(messageTime);
                            mi.setSessionMsg(message);
                            adapter.notifyItemChanged(pos);
                            break;
                        }
                        pos++;
                    }
                }
            }
            ignoreSenderId = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBind(MQTTService service) {
        for(User u : GlobalInfo.guardianshipList) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                    GlobalInfo.TOPIC_CHATTING, u.getPhoneNumber(), GlobalInfo.user.getPhoneNumber(),
                    1, Application.ACTION_CHAT_MESSAGE_RECEIVED
            ));
        }
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(msgArrivedReceiver);
        super.onDestroy();
    }

    private void updateArrivedMessage(ChatMessageRecord cmr){
        SessionMessageAdapter.MessageItem messageItem = null;
        for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
            if(mi.getSessionMessageRecord().getToUserId().equals(cmr.getChatterBId())){
                messageItem = mi;
                break;
            }
        }
        if(messageItem == null)
            return;
        messageItem.setSessionTime(cmr.getMessageTime());
        if(cmr.getMessageType().equals(ChatMessageRecord.MESSAGE_TYPE_TEXT))
            messageItem.setSessionMsg(cmr.getContent());
        messageItem.getSessionMessageRecord().setUnreadMessageNum(
                messageItem.getSessionMessageRecord().getUnreadMessageNum() + 1
        );
        messageItem.getSessionMessageRecord().save();
        Collections.sort(contactsSessionList);
        adapter.notifyDataSetChanged();
    }

    private class MessageArrivedReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!GlobalInfo.user.getPhoneNumber().equals(intent.getStringExtra(Application.BUNDLE_KEY_RECEIVER_ID)))
                return;
            String senderId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            if(senderId == null || senderId.isEmpty())
                return;
            if(senderId.equals(ignoreSenderId))
                return;
            if(viewRoot == null){
                SessionMessageRecord smr = DatabaseUtil.findSessionMessageRecord(GlobalInfo.user.getPhoneNumber(), senderId);
                if(smr == null)
                    smr = new SessionMessageRecord(GlobalInfo.user.getPhoneNumber(), senderId);
                smr.setUnreadMessageNum(smr.getUnreadMessageNum() + 1);
                smr.save();
                return;
            }
            ChatMessageRecord cmr = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE),
                    ChatMessageRecord.class);
            cmr.setChatterAId(GlobalInfo.user.getPhoneNumber());
            cmr.setChatterBId(senderId);
            cmr.setSenderId(senderId);
            updateArrivedMessage(cmr);
        }
    }

    private class MessageSentReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(viewRoot == null)
                return;
            String topicUserId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            if(!GlobalInfo.user.getPhoneNumber().equals(topicUserId))
                return;
            String receiverId = intent.getStringExtra(Application.BUNDLE_KEY_RECEIVER_ID);
            if(receiverId == null || receiverId.isEmpty())
                return;
            int pos = 0;
            for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
                if(mi.getSessionMessageRecord().getToUserId().equals(receiverId)){
                    mi.setSending(false);
                    adapter.notifyItemChanged(pos);
                    break;
                }
                pos++;
            }
        }
    }
}
