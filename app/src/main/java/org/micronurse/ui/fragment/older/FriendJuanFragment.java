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

import com.google.gson.JsonSyntaxException;

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
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

import java.util.Date;

public class FriendJuanFragment extends Fragment implements OnBindMQTTServiceListener {
    private View viewRoot;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Fragment[] friendJuanPages;
    private String[] pageTitles;

    private MessageArrivedReceiver msgArrivedReceiver;
    private MessageSentReceiver msgSentReceiver;
    private MessageSendStartReceiver msgSendStartReceiver;

    public FriendJuanFragment() {
        // Required empty public constructor
        msgArrivedReceiver = new MessageArrivedReceiver();
        msgSentReceiver = new MessageSentReceiver();
        msgSendStartReceiver = new MessageSendStartReceiver();
    }

    public static FriendJuanFragment getInstance(Context context){
        FriendJuanFragment fragment = new FriendJuanFragment();
        fragment.friendJuanPages = new Fragment[]{
                MessageFragment.getInstance(context),
                FriendContactsFragment.getInstance(context),
                MomentFragment.getInstance(context)
        };
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
    public void onBind(MQTTService service) {
        for(User u : GlobalInfo.guardianshipList) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(GlobalInfo.TOPIC_CHATTING,
                    u.getPhoneNumber(), GlobalInfo.user.getPhoneNumber(), 1, Application.ACTION_CHAT_MESSAGE_RECEIVED));
        }
        for(User u : GlobalInfo.Older.friendList) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(GlobalInfo.TOPIC_CHATTING,
                    u.getPhoneNumber(), GlobalInfo.user.getPhoneNumber(), 1, Application.ACTION_CHAT_MESSAGE_RECEIVED));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;

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
        viewRoot = inflater.inflate(R.layout.fragment_older_friend_juan, container, false);
        viewPager = (ViewPager) viewRoot.findViewById(R.id.tab_viewpager_friend_juan);
        viewPager.setAdapter(new FriendJuanPagerAdapter(getFragmentManager()));
        tabLayout = (TabLayout) viewRoot.findViewById(R.id.tab_friend_juan);
        tabLayout.setupWithViewPager(viewPager);
        return viewRoot;
    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(msgArrivedReceiver);
        getContext().unregisterReceiver(msgSentReceiver);
        getContext().unregisterReceiver(msgSendStartReceiver);
        super.onDestroy();
    }

    private class FriendJuanPagerAdapter extends FragmentPagerAdapter {
        public FriendJuanPagerAdapter(FragmentManager fm) {
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


    private class MessageArrivedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(GlobalInfo.user == null || !GlobalInfo.user.getPhoneNumber().equals(intent.getStringExtra(Application.BUNDLE_KEY_RECEIVER_ID)))
                return;
            String senderId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            if(senderId == null || senderId.isEmpty())
                return;
            try {
                ChatMessageRecord cmr = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE),
                        ChatMessageRecord.class);
                cmr.setChatterAId(GlobalInfo.user.getPhoneNumber());
                cmr.setChatterBId(senderId);
                cmr.setSenderId(senderId);
                for(Fragment f : friendJuanPages){
                    if(f instanceof MessageListener){
                        ((MessageListener) f).onMessageArrived(cmr);
                    }
                }
            }catch (JsonSyntaxException jse){
                jse.printStackTrace();
            }
        }
    }

    private class MessageSentReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String topicUserId = intent.getStringExtra(Application.BUNDLE_KEY_USER_ID);
            if(GlobalInfo.user == null || !GlobalInfo.user.getPhoneNumber().equals(topicUserId))
                return;
            String receiverId = intent.getStringExtra(Application.BUNDLE_KEY_RECEIVER_ID);
            if(receiverId == null || receiverId.isEmpty())
                return;
            for(Fragment f : friendJuanPages){
                if(f instanceof MessageListener){
                    ((MessageListener) f).onMessageSent(receiverId, intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE_ID));
                }
            }
        }
    }

    private class MessageSendStartReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(viewRoot == null)
                return;
            String receiverId = intent.getStringExtra(Application.BUNDLE_KEY_RECEIVER_ID);
            Date msgTime = new Date(intent.getLongExtra(Application.BUNDLE_KEY_MESSAGE_TIMESTAMP, -1));
            String msg = intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE);
            if(receiverId == null || receiverId.isEmpty())
                return;
            for(Fragment f : friendJuanPages){
                if(f instanceof MessageListener){
                    ((MessageListener) f).onMessageSendStart(receiverId, intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE_ID), msg, msgTime);
                }
            }
        }
    }
}
