package org.micronurse.ui.fragment.older;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.database.model.ChatMessageRecord;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.fragment.older.friendjuan.FriendContactsFragment;
import org.micronurse.ui.fragment.older.friendjuan.MessageFragment;
import org.micronurse.ui.fragment.older.friendjuan.MomentFragment;
import org.micronurse.ui.listener.MessageListener;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendJuanFragment extends Fragment implements OnBindMQTTServiceListener {
    private View rootView;
    @BindView(R.id.tab_viewpager_friend_juan)
    ViewPager viewPager;
    @BindView(R.id.tab_friend_juan)
    TabLayout tabLayout;

    private Fragment[] friendJuanPages;
    private String[] pageTitles;

    private MessageReceiver msgReceiver;

    public FriendJuanFragment() {
        // Required empty public constructor
    }

    public static FriendJuanFragment getInstance(Context context){
        FriendJuanFragment fragment = new FriendJuanFragment();
        fragment.friendJuanPages = new Fragment[]{
                MessageFragment.getInstance(context),
                FriendContactsFragment.getInstance(context),
                MomentFragment.getInstance(context)
        };
        return fragment;
    }

    @Override
    public void onBind(MQTTService service) {
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(Application.MQTT_TOPIC_CHATTING_FRIEND,
                    GlobalInfo.user.getUserId(), 1, Application.ACTION_CHAT_MESSAGE_RECEIVED));
        }
        service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(Application.MQTT_TOPIC_CHATTING_GUARDIANSHIP,
                    GlobalInfo.user.getUserId(), 1, Application.ACTION_CHAT_MESSAGE_RECEIVED));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;

        pageTitles = new String[]{
                getString(R.string.friend_message),
                getString(R.string.friend_contacts),
                getString(R.string.friend_moment)
        };
        if(friendJuanPages == null){
            friendJuanPages = new Fragment[]{
                new MessageFragment(), new FriendContactsFragment(), new MomentFragment()
            };
        }
        rootView = inflater.inflate(R.layout.fragment_older_friend_juan, container, false);
        ButterKnife.bind(this, rootView);
        viewPager.setAdapter(new FriendJuanPagerAdapter(getFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        msgReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED);
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

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    private class FriendJuanPagerAdapter extends FragmentPagerAdapter {
        FriendJuanPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return friendJuanPages[position];
        }

        @Override
        public int getCount() {
            return friendJuanPages.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
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
            for(Fragment f : friendJuanPages){
                if(f instanceof MessageListener){
                    if(intent.getAction().equals(Application.ACTION_CHAT_MESSAGE_RECEIVED_SAVED))
                        ((MessageListener) f).onMessageArrived(cmr);
                    else if(intent.getAction().equals(Application.ACTION_CHAT_MESSAGE_SENT_SAVED))
                        ((MessageListener) f).onMessageSent(cmr);
                    else if(intent.getAction().equals(Application.ACTION_CHAT_MESSAGE_SEND_START))
                        ((MessageListener) f).onMessageSendStart(cmr);
                }
            }
        }
    }
}
