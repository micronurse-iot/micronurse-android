package org.micronurse.ui.activity.older.main.monitor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;

import org.micronurse.R;

import java.util.Timer;
import java.util.TimerTask;

public class GoingoutMonitorFragment extends Fragment {
    private View viewRoot;
    private RecyclerView location;
    private RecyclerView home;
    private SwipeRefreshLayout refresh;
    //private List<location> locationList;
    //private List<location> homeList;
    private Timer scheduleTask;

    MapView mMapView;

    public GoingoutMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        viewRoot = inflater.inflate(R.layout.fragment_older_goingout_monitor, container, false);
        refresh = (SwipeRefreshLayout)viewRoot.findViewById(R.id.swipeLayout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        location = (RecyclerView) viewRoot.findViewById(R.id.location_list);
        location.setLayoutManager(new LinearLayoutManager(getContext()));
        location.setNestedScrollingEnabled(false);
        home = (RecyclerView) viewRoot.findViewById(R.id.home_list);
        home.setLayoutManager(new LinearLayoutManager(getContext()));
        home.setNestedScrollingEnabled(false);

        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            //TODO refresh
            @Override
            public void onRefresh() {
                updateLocation();
                updateHomeLocation();
            }
        });

        mMapView = (MapView) viewRoot.findViewById(R.id.bmapView);

        return viewRoot;
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        scheduleTask = new Timer();
        refresh.setRefreshing(true);
        scheduleTask.schedule(new TimerTask() {
            @Override
            public void run() {
                updateLocation();
                updateHomeLocation();
            }
        }, 0, 5000);
    }

    @Override
    public void onPause() {
        scheduleTask.cancel();
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        scheduleTask.cancel();
        super.onDestroy();
        mMapView.onDestroy();
    }

    private void updateLocation(){
        //TODO:
    }

    private void updateHomeLocation(){
        //TODO:
    }
}
