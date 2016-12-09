package org.micronurse.ui.fragment.older.friendjuan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.R;
import org.micronurse.adapter.SessionMessageAdapter;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.database.model.SessionMessageRecord;
import org.micronurse.model.User;
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.ui.listener.MessageListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MessageFragment extends Fragment implements MessageListener {
    private View viewRoot;
    private RecyclerView sessionListView;
    private LinkedList<SessionMessageAdapter.MessageItem> sessionList = new LinkedList<>();
    private SessionMessageAdapter adapter;

    public MessageFragment() {
        // Required empty public constructor
    }

    public static MessageFragment getInstance(Context context){
        return new MessageFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_friend_juan_message, container, false);
        sessionListView = (RecyclerView) viewRoot.findViewById(R.id.session_msg_list);
        sessionListView.setNestedScrollingEnabled(false);
        sessionListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<SessionMessageRecord> records = DatabaseUtil.findSessionMessageRecords(GlobalInfo.user.getUserId());
        adapter = new SessionMessageAdapter(getActivity(), sessionList, new SessionMessageAdapter.OnItemClickListener() {
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
        if(records == null || records.isEmpty()) {
            sessionListView.setAdapter(adapter);
            return viewRoot;
        }
        viewRoot.findViewById(R.id.txt_no_message).setVisibility(View.GONE);
        for(SessionMessageRecord smr : records){
            User u = GlobalInfo.findUserById(smr.getToUserId());
            if(u != null){
                List<ChatMessageRecord> chatRecords = DatabaseUtil.findChatMessageRecords(GlobalInfo.user.getUserId(),
                        smr.getToUserId(), new Date(), 1);
                if(chatRecords != null && !chatRecords.isEmpty()) {
                    if (chatRecords.get(0).getMessageType().equals(ChatMessageRecord.MESSAGE_TYPE_TEXT)) {
                        sessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(), u.getNickname(),
                                chatRecords.get(0).getMessageTime(), chatRecords.get(0).getContent(), smr));
                    }
                }
            }
        }
        Collections.sort(sessionList);
        sessionListView.setAdapter(adapter);
        return viewRoot;
    }

    private void updateArrivedMessage(ChatMessageRecord cmr){
        viewRoot.findViewById(R.id.txt_no_message).setVisibility(View.GONE);
        SessionMessageAdapter.MessageItem messageItem = null;
        for(SessionMessageAdapter.MessageItem mi : sessionList){
            if(mi.getSessionMessageRecord().getToUserId() == cmr.getChatterBId()){
                messageItem = mi;
                break;
            }
        }
        if(messageItem == null) {
            addNewSessionMessage(new SessionMessageRecord(GlobalInfo.user.getUserId(), cmr.getChatterBId(), 1),
                    cmr.getMessageTime(), cmr.getContent(), false);
            return;
        }
        messageItem.setSessionTime(cmr.getMessageTime());
        messageItem.setSessionMsg(cmr.getLiteralContent());
        messageItem.getSessionMessageRecord().setUnreadMessageNum(
                messageItem.getSessionMessageRecord().getUnreadMessageNum() + 1
        );
        messageItem.getSessionMessageRecord().save();
        Collections.sort(sessionList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        if(GlobalInfo.currentChatReceiverId != null){
            int pos = 0;
            for(SessionMessageAdapter.MessageItem mi : sessionList){
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
    public void onMessageArrived(ChatMessageRecord cmr) {
        if(viewRoot == null) {
            SessionMessageRecord smr = DatabaseUtil.findSessionMessageRecord(GlobalInfo.user.getUserId(), cmr.getChatterBId());
            if(smr == null){
                smr = new SessionMessageRecord(GlobalInfo.user.getUserId(), cmr.getChatterBId());
            }
            smr.setUnreadMessageNum(smr.getUnreadMessageNum() + 1);
            smr.save();
            return;
        }
        updateArrivedMessage(cmr);
    }

    @Override
    public void onMessageSent(int receiverId, String messageId) {
        if(viewRoot == null)
            return;
        int pos = 0;
        for(SessionMessageAdapter.MessageItem mi : sessionList){
            if(mi.getSessionMessageRecord().getToUserId() == receiverId){
                mi.setSending(false);
                adapter.notifyItemChanged(pos);
                break;
            }
            pos++;
        }
    }

    @Override
    public void onMessageSendStart(int receiverId, String messageId, String message, Date messageTime) {
        if(viewRoot == null)
            return;
        int pos = 0;
        for(SessionMessageAdapter.MessageItem mi : sessionList){
            if(mi.getSessionMessageRecord().getToUserId() == receiverId){
                mi.setSessionTime(messageTime);
                mi.setSessionMsg(message);
                mi.setSending(true);
                Collections.sort(sessionList);
                adapter.notifyDataSetChanged();
                break;
            }
            pos++;
        }
        if(pos >= sessionList.size()){
            addNewSessionMessage(new SessionMessageRecord(GlobalInfo.user.getUserId(), receiverId, 0),
                    messageTime, message, true);
        }
    }

    private void addNewSessionMessage(SessionMessageRecord smr, Date messageTime, String textMessage, boolean sending){
        viewRoot.findViewById(R.id.txt_no_message).setVisibility(View.GONE);
        User u = GlobalInfo.findUserById(smr.getToUserId());
        if(u != null){
            smr.save();
            sessionList.addFirst(new SessionMessageAdapter.MessageItem(
                    u.getPortrait(), u.getNickname(), messageTime, textMessage, smr, sending));
            Collections.sort(sessionList);
            adapter.notifyDataSetChanged();
        }
    }
}
