package org.micronurse.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.fragment.monitor.FamilyMonitorFragment;
import org.micronurse.ui.fragment.monitor.GoingoutMonitorFragment;
import org.micronurse.ui.fragment.monitor.HealthMonitorFragment;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.ui.widget.ViewPager;
import org.micronurse.util.GlobalInfo;

public class MonitorFragment extends Fragment implements OnBindMQTTServiceListener{
    private View viewRoot;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private OnFullScreenListener fullScreenListener;
    private Fragment currentFragment;
    private Fragment[] monitorPages;
    private String[] pageTitles;

    public MonitorFragment() {}

    public static MonitorFragment getInstance(Context context){
        MonitorFragment fragment = new MonitorFragment();
        fragment.monitorPages = new Fragment[]{
                FamilyMonitorFragment.getInstance(context),
                HealthMonitorFragment.getInstance(context),
                GoingoutMonitorFragment.getInstance(context)
        };
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(viewRoot != null)
            return viewRoot;

        if(monitorPages == null) {
            monitorPages = new Fragment[]{
                    FamilyMonitorFragment.getInstance(getContext()),
                    HealthMonitorFragment.getInstance(getContext()),
                    GoingoutMonitorFragment.getInstance(getContext())
            };
        }
        pageTitles = new String[]{
                getString(R.string.action_family_monitor),
                getString(R.string.action_health_monitor),
                getString(R.string.action_going_out_monitor),
        };
        ((GoingoutMonitorFragment)monitorPages[2]).setOnFullScreenListener(new OnFullScreenListener() {
            @Override
            public void onEnterFullScreen() {
                viewPager.setPagingEnabled(false);
                tabLayout.setVisibility(View.GONE);
                if(fullScreenListener != null)
                    fullScreenListener.onEnterFullScreen();
            }

            @Override
            public void onExitFullScreen() {
                viewPager.setPagingEnabled(true);
                tabLayout.setVisibility(View.VISIBLE);
                if(fullScreenListener != null)
                    fullScreenListener.onExitFullScreen();
            }
        });
        viewRoot = inflater.inflate(R.layout.fragment_monitor, container, false);
        viewPager = (ViewPager) viewRoot.findViewById(R.id.tab_viewpager_monitor);
        viewPager.setAdapter(new MonitorPagerAdapter(getFragmentManager()));
        viewPager.addOnPageChangeListener(new android.support.v4.view.ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(currentFragment == null)
                    currentFragment = monitorPages[0];
                currentFragment.onHiddenChanged(true);
                currentFragment = monitorPages[position];
                currentFragment.onHiddenChanged(false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        tabLayout = (TabLayout) viewRoot.findViewById(R.id.tab_monitor);
        tabLayout.setupWithViewPager(viewPager);
        return viewRoot;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(currentFragment != null)
            currentFragment.onHiddenChanged(false);
        else
            currentFragment = monitorPages[0];
    }

    @Override
    public void onBind(MQTTService service) {
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                    GlobalInfo.TOPIC_SENSOR_DATA_REPORT, GlobalInfo.user.getPhoneNumber(), 1, Application.ACTION_SENSOR_DATA_REPORT
            ));
        }else{
            if(GlobalInfo.guardianshipList != null){
                for(User u : GlobalInfo.guardianshipList){
                    service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                            GlobalInfo.TOPIC_SENSOR_DATA_REPORT, u.getPhoneNumber(), 1, Application.ACTION_SENSOR_DATA_REPORT
                    ));
                }
            }
        }
    }

    @Override
    public void onPause() {
        currentFragment.onHiddenChanged(true);
        super.onPause();
    }

    public void setOnFullScreenListener(OnFullScreenListener fullScreenListener) {
        this.fullScreenListener = fullScreenListener;
    }

    private class MonitorPagerAdapter extends FragmentPagerAdapter{
        public MonitorPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return monitorPages[position];
        }

        @Override
        public int getCount() {
            return monitorPages.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pageTitles[position];
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(currentFragment != null)
            currentFragment.onHiddenChanged(hidden);
        super.onHiddenChanged(hidden);
    }

}
