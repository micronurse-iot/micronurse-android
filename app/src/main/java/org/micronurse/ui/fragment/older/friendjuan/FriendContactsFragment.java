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
import org.micronurse.model.User;
import org.micronurse.ui.activity.ChatActivity;
import org.micronurse.util.GlobalInfo;

public class FriendContactsFragment extends Fragment {
    private View viewRoot;
    private ViewGroup listContainer;
    private SwipeRefreshLayout refresh;
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
        if(viewRoot != null)
            return viewRoot;

        viewRoot = inflater.inflate(R.layout.fragment_friend_juan_contacts, container, false);
        refresh = (SwipeRefreshLayout) viewRoot.findViewById(R.id.refresh_layout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContactList();
            }
        });
        listContainer = (ViewGroup) viewRoot.findViewById(R.id.contacts_list_container);

        refreshContactList();
        return viewRoot;
    }

    private void refreshContactList(){
        //TODO: Refresh contact list without removing the whole tree view.
        listContainer.removeAllViews();
        treeRoot = TreeNode.root();
        AndroidTreeView atv = new AndroidTreeView(getActivity(), treeRoot);
        guardianListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_contacts_32dp, getString(R.string.guardians) + " (" + GlobalInfo.guardianshipList.size() + ')'))
                .setViewHolder(new ContactListRootHolder(getActivity(), atv));
        for(final User u : GlobalInfo.guardianshipList){
            TreeNode node = new TreeNode(new ContactListContactHolder.IconTextItem(u.getPortrait(), u.getNickname()))
                            .setViewHolder(new ContactListContactHolder(getActivity(), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra(ChatActivity.BUNDLE_KEY_RECEIVER_ID, u.getUserId());
                                    startActivity(intent);
                                }
                            }));
            guardianListRoot.addChild(node);
        }
        olderFriendListRoot = new TreeNode(new ContactListRootHolder.IconTextItem(R.drawable.ic_friend_32dp, getString(R.string.friends) + " (" + GlobalInfo.Older.friendList.size() + ')'))
                .setViewHolder(new ContactListRootHolder(getActivity(), atv));
        for(final User u : GlobalInfo.Older.friendList){
            TreeNode node = new TreeNode(new ContactListContactHolder.IconTextItem(u.getPortrait(), u.getNickname()))
                    .setViewHolder(new ContactListContactHolder(getActivity(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(), ChatActivity.class);
                            intent.putExtra(ChatActivity.BUNDLE_KEY_RECEIVER_ID, u.getUserId());
                            startActivity(intent);
                        }
                    }));
            olderFriendListRoot.addChild(node);
        }
        treeRoot.addChildren(guardianListRoot, olderFriendListRoot);
        atv.setDefaultAnimation(true);
        listContainer.addView(atv.getView());
        refresh.setRefreshing(false);
    }
}
