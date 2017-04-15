package org.micronurse.ui.fragment.older.friendjuan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.ui.listener.MessageListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MessageFragment extends Fragment implements MessageListener {
    private View rootView;
    @BindView(R.id.session_msg_list)
    RecyclerView sessionListView;

    private LinkedList<SessionMessageAdapter.MessageItem> sessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;

    private UpdateSessionReceiver sessionReceiver;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment getInstance(Context context) {
        MessageFragment fragment = new MessageFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_friend_juan_message, container, false);
        ButterKnife.bind(this, rootView);

        sessionListView.setNestedScrollingEnabled(false);
        sessionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<SessionRecord> records = DatabaseUtil.findAllSessionRecords(GlobalInfo.user.getUserId());
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

        return rootView;
    }

    private void addNewSession(SessionRecord session){
        SessionMessageAdapter.MessageItem item = new SessionMessageAdapter.MessageItem();
        updateSessionInfo(item, session, true);
        if(item.getSession() == null)
            return;
        sessionList.addFirst(item);
        adapter.notifyItemInserted(0);
        ButterKnife.findById(rootView, R.id.txt_no_message).setVisibility(View.GONE);
    }

    @Override
    public void onMessageArrived(ChatMessageRecord cmr) {
        if(rootView == null)
            return;
        handleMsg(cmr);
    }

    @Override
    public void onMessageSent(ChatMessageRecord cmr) {
        handleMsg(cmr);
    }

    @Override
    public void onMessageSendStart(ChatMessageRecord cmr) {
        handleMsg(cmr);
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
    public void onDestroy() {
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
}
