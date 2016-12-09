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
    private View viewRoot;
    private RecyclerView sessionListView;
    private FloatingActionButton btnAddContact;
    private LinkedList<SessionMessageAdapter.MessageItem> contactsSessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;
    private MessageArrivedReceiver msgArrivedReceiver;
    private MessageSentReceiver msgSentReceiver;
    private MessageSendStartReceiver msgSendStartReceiver;

    public ContactsFragment() {
        // Required empty public constructor
        msgArrivedReceiver = new MessageArrivedReceiver();
        msgSentReceiver = new MessageSentReceiver();
        msgSendStartReceiver = new MessageSendStartReceiver();
    }

    public static ContactsFragment getInstance(Context context){
        ContactsFragment fragment = new ContactsFragment();
        IntentFilter filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED);
        filter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.msgArrivedReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SENT);
        filter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.msgSentReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SEND_START);
        filter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.msgSendStartReceiver, filter);
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
        List<SessionMessageRecord> records = DatabaseUtil.findSessionMessageRecords(GlobalInfo.user.getUserId());
        for(SessionMessageRecord smr : records){
            User u = GlobalInfo.findUserById(smr.getToUserId());
            if(u != null){
                List<ChatMessageRecord> chatRecords = DatabaseUtil.findChatMessageRecords(GlobalInfo.user.getUserId(),
                        smr.getToUserId(), new Date(), 1);
                if(chatRecords != null && !chatRecords.isEmpty()) {
                    if (chatRecords.get(0).getMessageType().equals(ChatMessageRecord.MESSAGE_TYPE_TEXT)) {
                        contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(),
                                chatRecords.get(0).getMessageTime(), chatRecords.get(0).getContent(), smr));
                        continue;
                    }
                }
                contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(),
                        null, null, smr));
            }
        }
        for(User u : GlobalInfo.guardianshipList) {
            boolean findFlag = false;
            for (SessionMessageAdapter.MessageItem mi : contactsSessionList) {
                if (u.getUserId() == mi.getSessionMessageRecord().getToUserId()) {
                    findFlag = true;
                    break;
                }
            }
            if(!findFlag){
                SessionMessageRecord smr = new SessionMessageRecord(GlobalInfo.user.getUserId(), u.getUserId());
                smr.save();
                contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(), smr));
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
                startActivity(intent);
            }
        });
        sessionListView.setAdapter(adapter);

        return viewRoot;
    }

    @Override
    public void onBind(MQTTService service) {
        for(User u : GlobalInfo.guardianshipList) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                    GlobalInfo.TOPIC_CHATTING, u.getUserId(), GlobalInfo.user.getUserId(),
                    1, Application.ACTION_CHAT_MESSAGE_RECEIVED
            ));
        }
    }

    @Override
    public void onResume() {
        if(GlobalInfo.currentChatReceiverId != null){
            int pos = 0;
            for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
                if(mi.getSessionMessageRecord().getToUserId() == GlobalInfo.currentChatReceiverId){
                    mi.getSessionMessageRecord().setUnreadMessageNum(0);
                    mi.getSessionMessageRecord().save();
                    adapter.notifyItemChanged(pos);
                    break;
                }
                pos++;
            }
            GlobalInfo.currentChatReceiverId = null;
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(msgArrivedReceiver);
        getContext().unregisterReceiver(msgSentReceiver);
        getContext().unregisterReceiver(msgSendStartReceiver);
        super.onDestroy();
    }

    private void updateArrivedMessage(ChatMessageRecord cmr){
        SessionMessageAdapter.MessageItem messageItem = null;
        for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
            if(mi.getSessionMessageRecord().getToUserId() == cmr.getChatterBId()){
                messageItem = mi;
                break;
            }
        }
        if(messageItem == null)
            return;
        messageItem.setSessionTime(cmr.getMessageTime());
        messageItem.setSessionMsg(cmr.getLiteralContent());
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
            if(GlobalInfo.user == null || GlobalInfo.user.getUserId() != intent.getIntExtra(Application.BUNDLE_KEY_RECEIVER_ID, -1))
                return;
            int senderId = intent.getIntExtra(Application.BUNDLE_KEY_USER_ID, -1);
            if(senderId < 0)
                return;
            if(viewRoot == null){
                SessionMessageRecord smr = DatabaseUtil.findSessionMessageRecord(GlobalInfo.user.getUserId(), senderId);
                if(smr == null)
                    smr = new SessionMessageRecord(GlobalInfo.user.getUserId(), senderId);
                smr.setUnreadMessageNum(smr.getUnreadMessageNum() + 1);
                smr.save();
                return;
            }
            ChatMessageRecord cmr = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE),
                    ChatMessageRecord.class);
            cmr.setChatterAId(GlobalInfo.user.getUserId());
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
            int topicUserId = intent.getIntExtra(Application.BUNDLE_KEY_USER_ID, -1);
            if(GlobalInfo.user == null || GlobalInfo.user.getUserId() != topicUserId)
                return;
            int receiverId = intent.getIntExtra(Application.BUNDLE_KEY_RECEIVER_ID, -1);
            if(receiverId < 0)
                return;
            int pos = 0;
            for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
                if(mi.getSessionMessageRecord().getToUserId() == receiverId){
                    mi.setSending(false);
                    adapter.notifyItemChanged(pos);
                    break;
                }
                pos++;
            }
        }
    }

    private class MessageSendStartReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(viewRoot == null)
                return;
            int receiverId = intent.getIntExtra(Application.BUNDLE_KEY_RECEIVER_ID, -1);
            Date msgTime = new Date(intent.getLongExtra(Application.BUNDLE_KEY_MESSAGE_TIMESTAMP, -1));
            String msg = intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE);
            if(receiverId < 0)
                return;
            for(SessionMessageAdapter.MessageItem mi : contactsSessionList){
                if(mi.getSessionMessageRecord().getToUserId() == receiverId){
                    mi.setSessionMsg(msg);
                    mi.setSessionTime(msgTime);
                    mi.setSending(true);
                    Collections.sort(contactsSessionList);
                    adapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    }
}
