package org.micronurse.ui.fragment.older.friendjuan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.micronurse.R;
import org.micronurse.adapter.ContactListContactHolder;
import org.micronurse.adapter.ContactListRootHolder;
import org.micronurse.database.model.SessionRecord;
import org.micronurse.model.User;
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.ui.listener.ContactListener;
import org.micronurse.util.GlobalInfo;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendContactsFragment extends Fragment implements ContactListener {
    private View rootView;
    @BindView(R.id.contacts_list_container)
    ViewGroup listContainer;
    private TreeNode treeRoot;
    private TreeNode guardianListRoot;
    private TreeNode olderFriendListRoot;

    public FriendContactsFragment() {
        // Required empty public constructor
    }

    public static FriendContactsFragment getInstance(Context context){
        return new FriendContactsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;

        rootView = inflater.inflate(R.layout.fragment_friend_juan_contacts, container, false);
        ButterKnife.bind(this, rootView);

        treeRoot = TreeNode.root();
        AndroidTreeView atv = new AndroidTreeView(getActivity(), treeRoot);
        guardianListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts_32dp, getString(R.string.guardians) + " (" + GlobalInfo.guardianshipList.size() + ')'))
                .setViewHolder(new ContactListRootHolder(getActivity(), atv));
        olderFriendListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_friend_32dp, getString(R.string.friends) + " (" + GlobalInfo.Older.friendList.size() + ')'))
                .setViewHolder(new ContactListRootHolder(getActivity(), atv));
        treeRoot.addChildren(guardianListRoot, olderFriendListRoot);
        atv.setDefaultAnimation(true);
        listContainer.addView(atv.getView());

        for(User u : GlobalInfo.guardianshipList)
            onAddGuardianship(u);
        for(User u : GlobalInfo.Older.friendList)
            onAddFriend(u);
        return rootView;
    }

    @Override
    public void onAddGuardianship(final User newContact) {
        TreeNode node = new TreeNode(new ContactListContactHolder.IconTextItem(newContact.getPortrait(), newContact.getNickname()))
                .setViewHolder(new ContactListContactHolder(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, SessionRecord.SESSION_TYPE_GUARDIANSHIP);
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, newContact.getUserId());
                        startActivity(intent);
                    }
                }));
        guardianListRoot.addChild(node);
    }

    @Override
    public void onAddFriend(final User newContact) {
        TreeNode node = new TreeNode(new ContactListContactHolder.IconTextItem(newContact.getPortrait(), newContact.getNickname()))
                .setViewHolder(new ContactListContactHolder(getActivity(), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_TYPE, SessionRecord.SESSION_TYPE_FRIEND);
                        intent.putExtra(ChatActivity.BUNDLE_KEY_SESSION_ID, newContact.getUserId());
                        startActivity(intent);
                    }
                }));
        olderFriendListRoot.addChild(node);
    }

    @OnClick(R.id.btn_add_friend)
    void onBtnAddFriendClick(View v){
        //TODO: Add friend
    }

    @OnClick(R.id.btn_add_guardian)
    void onBtnAddGuradianClick(View v){
        //TODO: Add guardianship
    }
}
