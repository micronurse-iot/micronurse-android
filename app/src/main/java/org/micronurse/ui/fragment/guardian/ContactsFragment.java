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
import org.micronurse.database.model.SessionRecord;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.ui.listener.ContactListener;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ContactsFragment extends Fragment implements OnBindMQTTServiceListener, ContactListener {
    private View rootView;
    @BindView(R.id.session_msg_list)
    RecyclerView sessionListView;
    @BindView(R.id.btn_add_older)
    FloatingActionButton btnAddOlder;

    private LinkedList<SessionMessageAdapter.MessageItem> sessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;

    private MessageReceiver msgReceiver;
    private UpdateSessionReceiver sessionReceiver;

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment getInstance(Context context) {
        ContactsFragment fragment = new ContactsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_guardian_contacts, container, false);
        ButterKnife.bind(this, rootView);

        sessionListView.setNestedScrollingEnabled(false);
        sessionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        sessionListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && btnAddOlder.isShown())
                    btnAddOlder.hide();
                else if(dy < 0 && !btnAddOlder.isShown())
                    btnAddOlder.show();
            }
        });
        List<SessionRecord> records = new ArrayList<>();
        for(User u : GlobalInfo.guardianshipList){
            SessionRecord sr = DatabaseUtil.findSessionRecord(GlobalInfo.user.getUserId(),
                    SessionRecord.SESSION_TYPE_GUARDIANSHIP, u.getUserId());
            if(sr != null)
                records.add(sr);
            else{
                sr = new SessionRecord(GlobalInfo.user.getUserId(),
                        SessionRecord.SESSION_TYPE_GUARDIANSHIP, u.getUserId());
                sr.save();
                records.add(sr);
            }
        }

        Collections.sort(records);
        Collections.reverse(records);
        adapter = new SessionMessageAdapter(getContext(), sessionList, new SessionMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, SessionMessageAdapter.MessageItem item) {
                switch (item.getSession().getSessionType()){
                    case SessionRecord.SESSION_TYPE_GROUP:
                    case SessionRecord.SESSION_TYPE_FRIEND:
                    case SessionRecord.SESSION_TYPE_GUARDIANSHIP:
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, item.getSession().getSessionType());
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, item.getSession().getSessionId());
                        startActivity(intent);
                        break;
                }
            }
        });
        sessionListView.setAdapter(adapter);
        for(SessionRecord sr : records)
            addNewSession(sr);

        //Register receiver
        sessionReceiver = new UpdateSessionReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_SESSION_UPDATE);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(sessionReceiver, filter);

        msgReceiver = new MessageReceiver();
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(msgReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SENT_SAVED);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(msgReceiver, filter);
        filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_SEND_START);
        filter.addCategory(getContext().getPackageName());
        getContext().registerReceiver(msgReceiver, filter);

        return rootView;
    }

    @OnClick(R.id.btn_add_older)
    void onBtnAddOlderClick(View v){
        //TODO: Add older
    }

    private void addNewSession(SessionRecord session){
        SessionMessageAdapter.MessageItem item = new SessionMessageAdapter.MessageItem();
        updateSessionInfo(item, session, true);
        if(item.getSession() == null)
            return;
        sessionList.addFirst(item);
        adapter.notifyItemInserted(0);
        ButterKnife.findById(rootView, R.id.txt_no_contact).setVisibility(View.GONE);
    }

    private void handleMsg(ChatMessageRecord cmr){
        for(SessionMessageAdapter.MessageItem mi : sessionList){
            if(mi.getSession().equals(cmr.getSession())){
                updateSessionInfo(mi, cmr.getSession(), false);
                Collections.sort(sessionList);
                adapter.notifyDataSetChanged();
                return;
            }
        }
        addNewSession(cmr.getSession());
    }

    private void updateSessionInfo(SessionMessageAdapter.MessageItem item, SessionRecord newSession, boolean fullUpdate){
        if(fullUpdate) {
            switch (newSession.getSessionType()) {
                case SessionRecord.SESSION_TYPE_GUARDIANSHIP:
                case SessionRecord.SESSION_TYPE_FRIEND:
                    User u = GlobalInfo.findUserById(newSession.getSessionId());
                    if (u == null)
                        return;
                    item.setPortrait(u.getPortrait());
                    item.setDisplayName(u.getNickname());
                    break;
            }
        }
        item.setSession(newSession);
    }

    @Override
    public void onAddGuardianship(User newContact) {
        User u = GlobalInfo.findUserById(newContact.getUserId());
        if(u == null)
            GlobalInfo.guardianshipList.add(newContact);
        SessionRecord sr = DatabaseUtil.findSessionRecord(GlobalInfo.user.getUserId(), SessionRecord.SESSION_TYPE_GUARDIANSHIP,
                newContact.getUserId());
        if(sr == null){
            sr = new SessionRecord(GlobalInfo.user.getUserId(), SessionRecord.SESSION_TYPE_GUARDIANSHIP,
                    newContact.getUserId());
            sr.save();
        }
        addNewSession(sr);
    }

    @Override
    public void onBind(MQTTService service) {
        service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(Application.MQTT_TOPIC_CHATTING_GUARDIANSHIP,
                GlobalInfo.user.getUserId(), 1, Application.ACTION_CHAT_MESSAGE_RECEIVED));
    }

    @Override
    public void onAddFriend(User newContact) {}

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(msgReceiver);
        getContext().unregisterReceiver(sessionReceiver);
        super.onDestroy();
    }

    private class UpdateSessionReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            long dbId = intent.getLongExtra(Application.BUNDLE_KEY_SESSION_DB_ID, -1);
            SessionRecord session = DatabaseUtil.findSessionRecordByDbId(dbId);
            for(SessionMessageAdapter.MessageItem mi : sessionList){
                if(mi.getSession().getId() == dbId){
                    if(session == null){
                        int index = sessionList.indexOf(mi);
                        sessionList.remove(index);
                        adapter.notifyItemRemoved(index);
                        return;
                    }else{
                        updateSessionInfo(mi, session, true);
                        Collections.sort(sessionList);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }
        }
    }

    private class MessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(GlobalInfo.user == null)
                return;
            ChatMessageRecord cmr = DatabaseUtil.findChatMessageByDbId(intent.getLongExtra(Application.BUNDLE_KEY_CHAT_MSG_DB_ID, -1));
            if(cmr == null)
                return;
            handleMsg(cmr);
        }
    }
}
