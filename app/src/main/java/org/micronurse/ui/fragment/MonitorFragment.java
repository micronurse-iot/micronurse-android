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

import com.google.gson.JsonSyntaxException;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.model.RawSensorData;
import org.micronurse.model.User;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.fragment.monitor.FamilyMonitorFragment;
import org.micronurse.ui.fragment.monitor.GoingoutMonitorFragment;
import org.micronurse.ui.fragment.monitor.HealthMonitorFragment;
import org.micronurse.ui.listener.OnBindMQTTServiceListener;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.ui.listener.OnSensorDataReceivedListener;
import org.micronurse.ui.widget.ViewPager;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;

public class MonitorFragment extends Fragment implements OnBindMQTTServiceListener{
    private View viewRoot;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private OnFullScreenListener fullScreenListener;
    private Fragment[] monitorPages;
    private String[] pageTitles;
    private SensorDataReceiver receiver;

    public MonitorFragment() {
        receiver = new SensorDataReceiver();
    }

    public static MonitorFragment getInstance(Context context){
        MonitorFragment fragment = new MonitorFragment();
        fragment.monitorPages = new Fragment[]{
                FamilyMonitorFragment.getInstance(context),
                HealthMonitorFragment.getInstance(context),
                GoingoutMonitorFragment.getInstance(context)
        };
        IntentFilter intentFilter = new IntentFilter(Application.ACTION_SENSOR_DATA_REPORT);
        intentFilter.addCategory(context.getPackageName());
        context.registerReceiver(fragment.receiver, intentFilter);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (viewRoot != null)
            return viewRoot;

        if (monitorPages == null) {
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
        ((GoingoutMonitorFragment) monitorPages[2]).setOnFullScreenListener(new OnFullScreenListener() {
            @Override
            public void onEnterFullScreen() {
                viewPager.setPagingEnabled(false);
                tabLayout.setVisibility(View.GONE);
                if (fullScreenListener != null)
                    fullScreenListener.onEnterFullScreen();
            }

            @Override
            public void onExitFullScreen() {
                viewPager.setPagingEnabled(true);
                tabLayout.setVisibility(View.VISIBLE);
                if (fullScreenListener != null)
                    fullScreenListener.onExitFullScreen();
            }
        });
        viewRoot = inflater.inflate(R.layout.fragment_monitor, container, false);
        viewPager = (ViewPager) viewRoot.findViewById(R.id.tab_viewpager_monitor);
        viewPager.setAdapter(new MonitorPagerAdapter(getFragmentManager()));
        tabLayout = (TabLayout) viewRoot.findViewById(R.id.tab_monitor);
        tabLayout.setupWithViewPager(viewPager);
        return viewRoot;
    }

    @Override
    public void onBind(MQTTService service) {
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                    GlobalInfo.TOPIC_SENSOR_DATA_REPORT, GlobalInfo.user.getUserId(), 1, Application.ACTION_SENSOR_DATA_REPORT
            ));
        }else{
            if(GlobalInfo.guardianshipList != null){
                for(User u : GlobalInfo.guardianshipList){
                    service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                            GlobalInfo.TOPIC_SENSOR_DATA_REPORT, u.getUserId(), 1, Application.ACTION_SENSOR_DATA_REPORT
                    ));
                }
            }
        }
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
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    private class SensorDataReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(GlobalInfo.user == null)
                return;
            int userId = intent.getIntExtra(Application.BUNDLE_KEY_USER_ID, -1);
            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER &&
                    GlobalInfo.user.getUserId() != userId)
                return;
            else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                    (GlobalInfo.Guardian.monitorOlder == null || GlobalInfo.Guardian.monitorOlder.getUserId() != userId))
                return;
            try {
                RawSensorData rawSensorData = GsonUtil.getGson().fromJson(intent.getStringExtra(Application.BUNDLE_KEY_MESSAGE), RawSensorData.class);
                for(Fragment f : monitorPages){
                    if(f instanceof OnSensorDataReceivedListener)
                        ((OnSensorDataReceivedListener) f).onSensorDataReceived(rawSensorData);
                }
            }catch (JsonSyntaxException e){
                e.printStackTrace();
            }
        }
    }

}
