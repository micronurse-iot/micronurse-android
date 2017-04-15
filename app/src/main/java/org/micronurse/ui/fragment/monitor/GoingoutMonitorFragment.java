package org.micronurse.ui.fragment.monitor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import org.micronurse.R;
import org.micronurse.net.PublicResultCode;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.result.GPSDataListResult;
import org.micronurse.net.model.result.HomeLocationResult;
import org.micronurse.net.model.result.Result;
import org.micronurse.model.GPS;
import org.micronurse.model.Sensor;
import org.micronurse.model.User;
import org.micronurse.ui.activity.older.SettingHomeLocationActivity;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.ui.listener.OnSensorDataReceivedListener;
import org.micronurse.util.DateTimeUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ImageUtil;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GoingoutMonitorFragment extends Fragment implements OnSensorDataReceivedListener {
    private static final int REQUEST_CODE_SETTING_HOME_LOCATION = 2333;

    private View rootView;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refresh;
    @BindView(R.id.btn_fullscreen)
    FloatingActionButton btnFullScreen;
    @BindView(R.id.btn_set_home_location)
    ImageButton btnSetHomeLocation;
    @BindView(R.id.bmap)
    MapView mMapView;
    @BindView(R.id.txt_data_time)
    TextView txtUpdateTime;
    @BindView(R.id.txt_current_location)
    TextView txtOlderLocation;
    @BindView(R.id.txt_home_location)
    TextView txtHomeLocation;

    private OnFullScreenListener fullScreenListener;
    private boolean isFullScreen = false;

    private GPS olderLocation;
    private BaiduMap baiduMap;
    private MarkerOptions olderMarkerOptions;
    private Marker olderMarker;
    private String updateLocationURL;
    private Marker homeMarker;
    private MarkerOptions homeMarkerOptions;
    private GPS homeLocation;

    public GoingoutMonitorFragment(){
        // Required empty public constructor
    }

    public static GoingoutMonitorFragment getInstance(Context context){
        return new GoingoutMonitorFragment();
    }

    private void updateURL(){
        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_OLDER:
                updateLocationURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        Sensor.SENSOR_TYPE_GPS, String.valueOf(1));
                break;
            case  User.ACCOUNT_TYPE_GUARDIAN:
                if(GlobalInfo.Guardian.monitorOlder == null){
                    refresh.setEnabled(false);
                    break;
                }
                updateLocationURL = HttpApi.getApiUrl(HttpApi.SensorAPI.LATEST_SENSOR_DATA,
                        String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()),
                        Sensor.SENSOR_TYPE_GPS, String.valueOf(1));
                break;
            default:
                refresh.setEnabled(false);
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if(rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.fragment_goingout_monitor, container, false);
        ButterKnife.bind(this, rootView);

        refresh.setColorSchemeResources(R.color.colorAccent);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateLocation();
            }
        });

        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN){
            btnSetHomeLocation.setVisibility(View.GONE);
            ((TextView) ButterKnife.findById(rootView, R.id.txt_older_location)).setText(R.string.older_location);
        }

        mMapView = (MapView) rootView.findViewById(R.id.bmap);
        mMapView.showZoomControls(false);
        baiduMap = mMapView.getMap();
        baiduMap.getUiSettings().setAllGesturesEnabled(false);
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));

        olderMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_location_red)))
                .draggable(false);
        homeMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(getContext(), R.drawable.ic_home_indigo_32dp)))
                .draggable(false);

        updateURL();
        if(refresh.isEnabled()){
            refresh.setRefreshing(true);
            updateLocation();
        }
        updateHomeLocation();
        return rootView;
    }

    @OnClick(R.id.btn_set_home_location)
    void onBtnSetHomeLocClick(View v){
        Intent intent = new Intent();
        intent.setClass(GoingoutMonitorFragment.this.getContext(), SettingHomeLocationActivity.class);
        if(homeLocation != null){
            intent.putExtra(SettingHomeLocationActivity.BUNDLE_HOME_LATITUDE, homeLocation.getLatitude());
            intent.putExtra(SettingHomeLocationActivity.BUNDLE_HOME_LONGITUDE, homeLocation.getLongitude());
            intent.putExtra(SettingHomeLocationActivity.BUNDLE_HOME_ADDR, homeLocation.getAddress());
        }
        GoingoutMonitorFragment.this.startActivityForResult(intent, REQUEST_CODE_SETTING_HOME_LOCATION);
    }

    @OnClick(R.id.btn_fullscreen)
    void onBtnFullscreen(View v){
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
        super.onDestroy();
    }

    private void updateLocation(){
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), updateLocationURL, Request.Method.GET, GlobalInfo.token, null,
                new HttpApiJsonListener<GPSDataListResult>(GPSDataListResult.class) {
                    @Override
                    public void onResponse() {
                        refresh.setRefreshing(false);
                    }

                    @Override
                    public void onDataResponse(GPSDataListResult data) {
                        updateLocation(data.getDataList().get(0));
                    }

                    @Override
                    public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                        return errorInfo.getResultCode() == PublicResultCode.SENSOR_DATA_NOT_FOUND;
                    }
                }));
    }

    private synchronized void updateLocation(GPS gps) {
        if (olderLocation != null) {
            if (gps.getTimestamp().getTime() < olderLocation.getTimestamp().getTime())
                return;
        }
        ButterKnife.findById(rootView, R.id.txt_no_data).setVisibility(View.GONE);
        ButterKnife.findById(rootView, R.id.location_area).setVisibility(View.VISIBLE);
        LatLng pos = new LatLng(gps.getLatitude(), gps.getLongitude());
        if (!isFullScreen || olderLocation == null)
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(pos));
        olderLocation = gps;
        if (olderMarker != null)
            olderMarker.setPosition(pos);
        else
            olderMarker = (Marker) baiduMap.addOverlay(olderMarkerOptions.position(pos));
        txtUpdateTime.setText(DateTimeUtil.convertTimestamp(getContext(), olderLocation.getTimestamp(), true, true, true));
        txtOlderLocation.setText(olderLocation.getAddress());
    }

    private void updateHomeLocation(double latitude, double longitude, String address){
        homeLocation = new GPS(new Date(), longitude, latitude, address);
        LatLng homeAddress = new LatLng(homeLocation.getLatitude(), homeLocation.getLongitude());
        if(homeMarker != null)
            homeMarker.setPosition(homeAddress);
        else
            homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions.position(homeAddress));
        txtHomeLocation.setText(address);
    }

    private void updateHomeLocation(){
        String url = null;
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
            url = HttpApi.getApiUrl(HttpApi.AccountAPI.HOME_ADDRESS);
        else if(GlobalInfo.Guardian.monitorOlder != null)
            url = HttpApi.getApiUrl(HttpApi.AccountAPI.HOME_ADDRESS, String.valueOf(GlobalInfo.Guardian.monitorOlder.getUserId()));
        if(url == null)
            return;
        HttpApi.startRequest(new HttpApiJsonRequest(getActivity(), url, Request.Method.GET,
                GlobalInfo.token, null, new HttpApiJsonListener<HomeLocationResult>(HomeLocationResult.class) {
            @Override
            public void onDataResponse(HomeLocationResult data) {
                updateHomeLocation(data.getLatitude(), data.getLongitude(), data.getAddress());
            }

            @Override
            public boolean onErrorDataResponse(int statusCode, Result errorInfo) {
                return errorInfo.getResultCode() == PublicResultCode.HOME_ADDRESS_NOT_EXIST;
            }
        }));
    }

    public void setOnFullScreenListener(OnFullScreenListener fullScreenListener) {
        this.fullScreenListener = fullScreenListener;
    }

    @Override
    public void onSensorDataReceived(Sensor sensor) {
        if(rootView == null)
            return;
        if(sensor instanceof GPS)
            updateLocation((GPS) sensor);
    }
}
