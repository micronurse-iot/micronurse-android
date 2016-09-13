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
    private View viewRoot;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private OnFullScreenListener fullScreenListener;
    private Fragment currentFragment;
    private final Fragment[] monitorPages;
    private String[] pageTitles;

    public MonitorFragment() {
        monitorPages = new Fragment[]{
                new FamilyMonitorFragment(),
                new HealthMonitorFragment(),
                new GoingoutMonitorFragment()
        };
        Bundle b = new Bundle();
        b.putBoolean(FamilyMonitorFragment.BUNDLE_KEY_FIRST_DISPLAY, true);
        monitorPages[0].setArguments(b);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(viewRoot != null)
            return viewRoot;
        pageTitles = new String[]{
                getString(R.string.action_family_monitor),
                getString(R.string.action_health_monitor),
                getString(R.string.action_going_out_monitor),
        };
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
    }

    @Override
    public void onPause() {
        super.onPause();
        currentFragment.onHiddenChanged(true);
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
