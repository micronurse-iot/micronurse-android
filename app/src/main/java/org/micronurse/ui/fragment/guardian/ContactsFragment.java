package org.micronurse.ui.fragment.guardian;

import android.content.Context;
import android.content.Intent;
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

    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment getInstance(Context context){
        return new ContactsFragment();
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
        for(User u : GlobalInfo.guardianshipList){
            SessionMessageRecord smr = DatabaseUtil.findSessionMessageRecord(GlobalInfo.user.getPhoneNumber(),
                    u.getPhoneNumber());
            if(smr == null) {
                smr = new SessionMessageRecord(GlobalInfo.user.getPhoneNumber(), u.getPhoneNumber());
                smr.save();
            }
            List<ChatMessageRecord> recordList = DatabaseUtil.findChatMessageRecords(GlobalInfo.user.getPhoneNumber(),
                    u.getPhoneNumber(), new Date(), 1);
            ChatMessageRecord cmr = null;
            if(recordList != null && !recordList.isEmpty())
                cmr = recordList.get(0);
            if (cmr == null) {
                contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(),
                        u.getNickname(), null, null, smr));
            } else {
                String content = null;
                if (cmr.getMessageType().equals(ChatMessageRecord.MESSAGE_TYPE_TEXT))
                    content = cmr.getContent();
                contactsSessionList.add(new SessionMessageAdapter.MessageItem(u.getPortrait(),
                        u.getNickname(), cmr.getMessageTime(), content, smr));
            }
        }
        Collections.sort(contactsSessionList);
        adapter = new SessionMessageAdapter(getContext(), contactsSessionList, new SessionMessageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SessionMessageAdapter.MessageItem item) {
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
                    GlobalInfo.TOPIC_CHATTING, u.getPhoneNumber(), GlobalInfo.user.getPhoneNumber(),
                    1, Application.ACTION_CHAT_MESSAGE_RECEIVED
            ));
        }
    }
}
