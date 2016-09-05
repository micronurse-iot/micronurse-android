package org.micronurse.ui.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.R;
import org.micronurse.ui.fragment.monitor.FamilyMonitorFragment;
import org.micronurse.ui.fragment.monitor.GoingoutMonitorFragment;
import org.micronurse.ui.fragment.monitor.HealthMonitorFragment;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.ui.widget.ViewPager;

public class MonitorFragment extends Fragment {
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private OnFullScreenListener fullScreenListener;

    public MonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_monitor, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.tab_viewpager_monitor);
        viewPager.setAdapter(new MonitorPagerAdapter(getFragmentManager()));
        tabLayout = (TabLayout) v.findViewById(R.id.tab_monitor);
        tabLayout.setupWithViewPager(viewPager);
        return v;
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
            switch (position){
                case 0:
                    return new FamilyMonitorFragment();
                case 1:
                    return new HealthMonitorFragment();
                case 2:
                    GoingoutMonitorFragment gmf = new GoingoutMonitorFragment();
                    gmf.setOnFullScreenListener(new OnFullScreenListener() {
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
                    return gmf;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 0:
                    return getString(R.string.action_family_monitor);
                case 1:
                    return getString(R.string.action_health_monitor);
                case 2:
                    return getString(R.string.action_going_out_monitor);
            }
            return null;
        }
    }
}
