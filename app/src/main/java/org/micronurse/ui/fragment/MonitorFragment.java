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
import org.micronurse.util.SensorUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MonitorFragment extends Fragment implements OnBindMQTTServiceListener{
    private View rootView;
    @BindView(R.id.tab_viewpager_monitor)
    ViewPager viewPager;
    @BindView(R.id.tab_monitor)
    TabLayout tabLayout;
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
        if (rootView != null)
            return rootView;

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
        rootView = inflater.inflate(R.layout.fragment_monitor, container, false);
        ButterKnife.bind(this, rootView);
        viewPager.setAdapter(new MonitorPagerAdapter(getFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
        return rootView;
    }

    @Override
    public void onBind(MQTTService service) {
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                    Application.MQTT_TOPIC_SENSOR_DATA_REPORT, GlobalInfo.user.getUserId(), 1, Application.ACTION_SENSOR_DATA_REPORT
            ));
        }else{
            if(GlobalInfo.guardianshipList != null){
                for(User u : GlobalInfo.guardianshipList){
                    service.addMQTTAction(new MQTTService.MQTTSubscriptionAction(
                            Application.MQTT_TOPIC_SENSOR_DATA_REPORT, u.getUserId(), 1, Application.ACTION_SENSOR_DATA_REPORT
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
        public void onReceive(Context context, Intent mqttIntent) {
            if(GlobalInfo.user == null)
                return;
            long userId = mqttIntent.getLongExtra(MQTTService.BUNDLE_KEY_TOPIC_OWNER_ID, -1);
            if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER &&
                    GlobalInfo.user.getUserId() != userId)
                return;
            else if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                    (GlobalInfo.Guardian.monitorOlder == null || GlobalInfo.Guardian.monitorOlder.getUserId() != userId))
                return;
            try {
                RawSensorData rawSensorData = GsonUtil.getGson().fromJson(mqttIntent.getStringExtra(MQTTService.BUNDLE_KEY_MESSAGE), RawSensorData.class);
                for(Fragment f : monitorPages){
                    if(f instanceof OnSensorDataReceivedListener)
                        ((OnSensorDataReceivedListener) f).onSensorDataReceived(SensorUtil.parseRawSensorData(rawSensorData));
                }
            }catch (JsonSyntaxException e){
                e.printStackTrace();
            }
        }
    }

}
