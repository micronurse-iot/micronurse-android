package org.micronurse.ui.activity.older.main.monitor;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;


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
    private final String key = "EROcxOxeLvnewEAAR5hTIZpTBDWlCteD";

    private double latitude = 40.050966;// 纬度
    private double longitude = 116.303128;// 经度
    private LatLng hmPos = new LatLng(latitude, longitude);// 坐标
    private MapView mMapView;
    private BaiduMap baiduMap;
    private BMapManager mBMapManager = null;

    public GoingoutMonitorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;
        mBMapManager = new BMapManager();
        mBMapManager.init();
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
        baiduMap = mMapView.getMap();
        //设置缩放级别，默认级别为12
        MapStatusUpdate mapstatusUpdate = MapStatusUpdateFactory.zoomTo(19);;
        baiduMap.setMapStatus(mapstatusUpdate);

        //设置地图中心点，默认是天安门
        MapStatusUpdate mapstatusUpdatePoint = MapStatusUpdateFactory.newLatLng(hmPos);
        baiduMap.setMapStatus(mapstatusUpdatePoint );
        return viewRoot;
    }
    @Override
    public void onResume() {
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
        super.onResume();
    }

    @Override
    public void onPause() {
        scheduleTask.cancel();
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        scheduleTask.cancel();
        mMapView.onDestroy();
        super.onDestroy();
    }

    private void updateLocation(){
        //TODO:
    }

    private void updateHomeLocation(){
        //TODO:
    }
}
