package org.micronurse.ui.activity.guardian.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.micronurse.R;
import org.micronurse.ui.activity.guardian.main.monitor.FamilyMonitorFragment;
import org.micronurse.ui.activity.guardian.main.monitor.GoingoutMonitorFragment;
import org.micronurse.ui.activity.guardian.main.monitor.HealthMonitorFragment;

public class MonitorFragment extends Fragment {

    public MonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_monitor, container, false);
        ViewPager vp = (ViewPager) v.findViewById(R.id.tab_viewpager_monitor);
        vp.setAdapter(new MonitorPagerAdapter(getFragmentManager()));
        ((TabLayout)v.findViewById(R.id.tab_monitor)).setupWithViewPager(vp);
        return v;
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
                    return new GoingoutMonitorFragment();
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
