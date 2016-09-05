package org.micronurse.ui.fragment.monitor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;


import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.result.GPSDataListResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.GPS;
import org.micronurse.model.Sensor;
import org.micronurse.model.User;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ImageUtil;

import java.util.Timer;
import java.util.TimerTask;

public class GoingoutMonitorFragment extends Fragment {
    private View viewRoot;
    private SwipeRefreshLayout refresh;
    private FloatingActionButton btnFullScreen;
    private ImageButton btnSetHomeLocation;
    private OnFullScreenListener fullScreenListener;
    private boolean isFullScreen = false;

    private Timer scheduleTask;
    private GPS olderLocation;
    private MapView mMapView;
    private BaiduMap baiduMap;
    private MarkerOptions olderMarkerOptions;
    private Marker olderMarker;
    private GeoCoder geoCoder;
    private String updateLocationURL;

    public GoingoutMonitorFragment() {
        // Required empty public constructor
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    Log.e(GlobalInfo.LOG_TAG, "Geo reverse error:" + reverseGeoCodeResult.error);
                    ((TextView)viewRoot.findViewById(R.id.older_location)).setText(R.string.unknown_location);
                }else{
                    ((TextView)viewRoot.findViewById(R.id.older_location)).setText(reverseGeoCodeResult.getAddress());
                }
            }
        });
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
            updateLocationURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_GPS,
                    String.valueOf(1));
        else if(GlobalInfo.Guardian.monitorOlder != null){
            updateLocationURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_GPS, String.valueOf(1));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(viewRoot != null)
            return viewRoot;

        viewRoot = inflater.inflate(R.layout.fragment_goingout_monitor, container, false);
        refresh = (SwipeRefreshLayout) viewRoot.findViewById(R.id.swipeLayout);
        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateLocation();
            }
        });
        btnSetHomeLocation = (ImageButton) viewRoot.findViewById(R.id.btn_set_home_location);
        btnFullScreen = (FloatingActionButton) viewRoot.findViewById(R.id.btn_fullscreen);
        btnFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFullScreen){
                    isFullScreen = true;
                    btnFullScreen.setImageResource(R.drawable.ic_fullscreen_exit);
                    baiduMap.getUiSettings().setAllGesturesEnabled(true);
                    refresh.setEnabled(false);
                    mMapView.showZoomControls(true);
                    mMapView.getChildAt(2)
                            .setPadding(0, 0, getContext().getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                                    getContext().getResources().getDimensionPixelSize(R.dimen.map_zoom_control_padding_bottom));
                    if(fullScreenListener != null){
                        fullScreenListener.onEnterFullScreen();
                    }
                }else{
                    isFullScreen = false;
                    btnFullScreen.setImageResource(R.drawable.ic_fullscreen);
                    baiduMap.getUiSettings().setAllGesturesEnabled(false);
                    refresh.setEnabled(true);
                    mMapView.showZoomControls(false);
                    if(fullScreenListener != null){
                        fullScreenListener.onExitFullScreen();
                    }
                }
            }
        });

        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN){
            btnSetHomeLocation.setVisibility(View.GONE);
            ((TextView) viewRoot.findViewById(R.id.txt_older_location)).setText(R.string.older_location);
        }

        mMapView = (MapView) viewRoot.findViewById(R.id.bmapView);
        mMapView.showZoomControls(false);
        baiduMap = mMapView.getMap();
        baiduMap.getUiSettings().setAllGesturesEnabled(false);
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));

        olderMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_location_red)))
                .draggable(false);
        return viewRoot;
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN &&
                GlobalInfo.Guardian.monitorOlder == null){
            scheduleTask = null;
            viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.VISIBLE);
            viewRoot.findViewById(R.id.location_area).setVisibility(View.GONE);
            refresh.setEnabled(false);
        }else {
            scheduleTask = new Timer();
            refresh.setRefreshing(true);
            updateHomeLocation();
            scheduleTask.schedule(new TimerTask() {
                @Override
                public void run() {
                    updateLocation();
                }
            }, 0, 5000);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if(scheduleTask != null)
            scheduleTask.cancel();
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        geoCoder.destroy();
        super.onDestroy();
    }

    private void updateLocation(){
        new MicronurseAPI<GPSDataListResult>(getActivity(), updateLocationURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<GPSDataListResult>() {
            @Override
            public void onResponse(GPSDataListResult response) {
                refresh.setRefreshing(false);
                viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
                viewRoot.findViewById(R.id.location_area).setVisibility(View.VISIBLE);
                LatLng latLng = new LatLng(response.getDataList().get(0).getLatitude(), response.getDataList().get(0).getLongitude());
                if(!isFullScreen || olderLocation == null)
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
                olderLocation = response.getDataList().get(0);
                olderMarkerOptions.position(latLng);
                if(olderMarker != null)
                    olderMarker.remove();
                olderMarker = (Marker) baiduMap.addOverlay(olderMarkerOptions);
                ((TextView)viewRoot.findViewById(R.id.data_update_time)).setText(DateTimeUtil.convertTimestamp(getContext(),
                        olderLocation.getTimestamp()));
                geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(latLng));
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                refresh.setRefreshing(false);
            }
        }, GPSDataListResult.class, false, null).startRequest();
    }

    private void updateHomeLocation(){
        //TODO:
    }

    public void setOnFullScreenListener(OnFullScreenListener fullScreenListener) {
        this.fullScreenListener = fullScreenListener;
    }

}
