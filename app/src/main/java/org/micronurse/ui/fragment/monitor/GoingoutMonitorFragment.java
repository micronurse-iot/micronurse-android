package org.micronurse.ui.fragment.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

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
import com.google.gson.JsonSyntaxException;

import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.result.GPSDataListResult;
import org.micronurse.http.model.result.GetHomeLocationResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.GPS;
import org.micronurse.model.RawSensorData;
import org.micronurse.model.Sensor;
import org.micronurse.model.User;
import org.micronurse.ui.activity.older.SettingHomeLocationActivity;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.ui.listener.OnSensorDataReceivedListener;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.GsonUtil;
import org.micronurse.util.ImageUtil;

public class GoingoutMonitorFragment extends Fragment  implements
        OnGetGeoCoderResultListener, OnSensorDataReceivedListener {
    private static final int REQUEST_CODE_SETTING_HOME_LOCATION = 2333;

    private View viewRoot;
    private SwipeRefreshLayout refresh;
    private FloatingActionButton btnFullScreen;
    private ImageButton btnSetHomeLocation;
    private OnFullScreenListener fullScreenListener;
    private boolean isFullScreen = false;

    private GPS olderLocation;
    private MapView mMapView;
    private BaiduMap baiduMap;
    private MarkerOptions olderMarkerOptions;
    private Marker olderMarker;
    private GeoCoder geoCoder;
    private String updateLocationURL;
    private Marker homeMarker;
    private MarkerOptions homeMarkerOptions;
    private LatLng homeAddress;

    public GoingoutMonitorFragment(){
        // Required empty public constructor
        geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(this);
    }

    public static GoingoutMonitorFragment getInstance(Context context){
        return new GoingoutMonitorFragment();
    }

    private void updateURL(){
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
            updateLocationURL = MicronurseAPI.getApiUrl(MicronurseAPI.OlderSensorAPI.LATEST_SENSOR_DATA, Sensor.SENSOR_TYPE_GPS,
                    String.valueOf(1));
        else if(GlobalInfo.Guardian.monitorOlder != null){
            updateLocationURL = MicronurseAPI.getApiUrl(MicronurseAPI.GuardianSensorAPI.LATEST_SENSOR_DATA,
                    GlobalInfo.Guardian.monitorOlder.getPhoneNumber(),
                    Sensor.SENSOR_TYPE_GPS, String.valueOf(1));
        }else{
            refresh.setEnabled(false);
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
        btnSetHomeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(GoingoutMonitorFragment.this.getContext(), SettingHomeLocationActivity.class);
                GoingoutMonitorFragment.this.startActivityForResult(intent, REQUEST_CODE_SETTING_HOME_LOCATION);

            }
        });
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
        homeMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_home_32dp)))
                .draggable(false);

        updateURL();
        if(refresh.isEnabled()){
            refresh.setRefreshing(true);
            updateLocation();
        }
        updateHomeLocation();
        return viewRoot;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_SETTING_HOME_LOCATION && resultCode == SettingHomeLocationActivity.RESULT_CODE_SET_HOME_LOCATION){
            updateHomeLocation();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
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
        new MicronurseAPI<>(getActivity(), updateLocationURL, Request.Method.GET, null, GlobalInfo.token, new Response.Listener<GPSDataListResult>() {
            @Override
            public void onResponse(GPSDataListResult response) {
                refresh.setRefreshing(false);
                updateLocation(response.getDataList().get(0));
            }
        }, new APIErrorListener() {
            @Override
            public void onErrorResponse(VolleyError err, Result result) {
                refresh.setRefreshing(false);
            }
        }, GPSDataListResult.class, false, null).startRequest();
    }

    private synchronized void updateLocation(GPS gps){
        if(olderLocation != null){
            if(gps.getTimestamp() < olderLocation.getTimestamp())
                return;
        }
        viewRoot.findViewById(R.id.txt_no_data).setVisibility(View.GONE);
        viewRoot.findViewById(R.id.location_area).setVisibility(View.VISIBLE);
        LatLng latLng = new LatLng(gps.getLatitude(), gps.getLongitude());
        if(!isFullScreen || olderLocation == null)
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
        olderLocation = gps;
        olderMarkerOptions.position(latLng);
        if(olderMarker != null)
            olderMarker.remove();
        olderMarker = (Marker) baiduMap.addOverlay(olderMarkerOptions);
        ((TextView)viewRoot.findViewById(R.id.data_update_time)).setText(DateTimeUtil.convertTimestamp(getContext(),
                olderLocation.getTimestamp()));
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                .location(latLng));
    }

    private void updateHomeLocation(){
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
            new MicronurseAPI<GetHomeLocationResult>(getActivity(), MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.GET_HOME_ADDRESS_FROME_OLDER, GlobalInfo.token), Request.Method.GET,
                    null, GlobalInfo.token, new Response.Listener<GetHomeLocationResult>() {
                @Override
                public void onResponse(GetHomeLocationResult response) {
                    homeAddress = new LatLng(response.getLatitude(), response.getLongitude());
                    homeMarkerOptions.position(homeAddress);
                    if(homeMarker != null)
                        homeMarker.remove();
                    homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions);
                    GeoCoder searchHome = GeoCoder.newInstance();
                    searchHome.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                        @Override
                        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

                        @Override
                        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                            ((TextView)viewRoot.findViewById(R.id.home_location)).setText(reverseGeoCodeResult.getAddress());
                        }
                    });
                    searchHome.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(homeAddress));
                }
            }, new APIErrorListener() {
                @Override
                public void onErrorResponse(VolleyError err, Result result) {
                    if (result != null) {
                        switch (result.getResultCode()) {
                            case PublicResultCode.HOME_LOCATION_UNSETTED:
                                ((TextView)viewRoot.findViewById(R.id.home_location)).setText(R.string.home_loaction_not_setting);
                                break;
                            default:
                                Toast.makeText(GoingoutMonitorFragment.this.getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, GetHomeLocationResult.class, false, null).startRequest();
        } else if(GlobalInfo.Guardian.monitorOlder != null){
            new MicronurseAPI<GetHomeLocationResult>(getActivity(), MicronurseAPI.getApiUrl(MicronurseAPI.AccountAPI.GET_HOME_ADDRESS_FROME_GUARDIAN, GlobalInfo.Guardian.monitorOlder.getPhoneNumber()),
                    Request.Method.GET, null, GlobalInfo.token, new Response.Listener<GetHomeLocationResult>() {
                @Override
                public void onResponse(GetHomeLocationResult response) {
                    homeAddress = new LatLng(response.getLatitude(), response.getLongitude());
                    homeMarkerOptions.position(homeAddress);
                    if(homeMarker != null)
                        homeMarker.remove();
                    homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions);
                    geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(homeAddress));
                }
            }, new APIErrorListener() {
                @Override
                public void onErrorResponse(VolleyError err, Result result) {
                    if (result != null) {
                        switch (result.getResultCode()) {
                            case PublicResultCode.HOME_LOCATION_UNSETTED:
                                ((TextView)viewRoot.findViewById(R.id.home_location)).setText(R.string.home_loaction_not_setting);
                                break;
                            case PublicResultCode.RELATION_NOT_EXIST:
                                Toast.makeText(GoingoutMonitorFragment.this.getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                Toast.makeText(GoingoutMonitorFragment.this.getContext(), result.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }, GetHomeLocationResult.class, false, null).startRequest();
        }
    }

    public void setOnFullScreenListener(OnFullScreenListener fullScreenListener) {
        this.fullScreenListener = fullScreenListener;
    }

    @Override
    public void onSensorDataReceived(RawSensorData rawSensorData) {
        if(viewRoot == null)
            return;
        if(rawSensorData.getSensorType().toLowerCase().equals(Sensor.SENSOR_TYPE_GPS)){
            String[] splitStr = rawSensorData.getValue().split(",", 2);
            if(splitStr.length != 2)
                return;
            updateLocation(new GPS(rawSensorData.getTimestamp(), Double.valueOf(splitStr[0]),
                    Double.valueOf(splitStr[1])));
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {}

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(GlobalInfo.LOG_TAG, "Geo reverse error:" + reverseGeoCodeResult.error);
            ((TextView)viewRoot.findViewById(R.id.older_location)).setText(R.string.unknown_location);
        }else{
            ((TextView) viewRoot.findViewById(R.id.older_location)).setText(reverseGeoCodeResult.getAddress());
        }
    }
}
