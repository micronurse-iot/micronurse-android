package org.micronurse.ui.fragment.older;

import android.content.Context;
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
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.fragment.older.friendjuan.FriendContactsFragment;
import org.micronurse.ui.fragment.older.friendjuan.MessageFragment;
import org.micronurse.ui.fragment.older.friendjuan.ShareFragment;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.util.GlobalInfo;

public class FriendJuanFragment extends Fragment implements OnBindMQTTServiceListener {
    private View viewRoot;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private final Fragment[] friendJuanPages;
    private String[] pageTitles;

    public FriendJuanFragment() {
        // Required empty public constructor
        friendJuanPages = new Fragment[3];
        friendJuanPages[0] = new MessageFragment();
        friendJuanPages[1] = new FriendContactsFragment();
        friendJuanPages[2] = new ShareFragment();
    }

    public static FriendJuanFragment getInstance(Context context){
        return new FriendJuanFragment();
    }

    @Override
    public void onBind(MQTTService service) {
        for(User u : GlobalInfo.guardianshipList) {
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
                getString(R.string.friend_share)
        };
        viewRoot = inflater.inflate(R.layout.fragment_older_friend_juan, container, false);
        viewPager = (ViewPager) viewRoot.findViewById(R.id.tab_viewpager_friend_juan);
        viewPager.setAdapter(new FriendJuanPagerAdapter(getFragmentManager()));
        tabLayout = (TabLayout) viewRoot.findViewById(R.id.tab_friend_juan);
        tabLayout.setupWithViewPager(viewPager);
        return viewRoot;
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
}
