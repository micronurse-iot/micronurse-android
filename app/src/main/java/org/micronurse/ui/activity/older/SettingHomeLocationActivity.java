package org.micronurse.ui.activity.older;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import org.micronurse.R;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.SaveHomeLocationRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ImageUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingHomeLocationActivity extends AppCompatActivity  implements
        OnGetGeoCoderResultListener {
    public static final int RESULT_CODE_SET_HOME_LOCATION = 666;
    public static final String BUNDLE_HOME_LONGITUDE = "HomeLongitude";
    public static final String BUNDLE_HOME_LATITUDE = "HomeLatitude";
    public static final String BUNDLE_HOME_ADDR = "HomeAddr";

    @BindView(R.id.edit_home_address)
    EditText editAddr;
    @BindView(R.id.bmap)
    MapView mMapView;
    private BaiduMap baiduMap;
    private MarkerOptions homeMarkerOptions;
    private Marker homeMarker;
    private GeoCoder mSearch;
    private Double mCurrentLatitude, mCurrentLongitude;


    //private SensorDataReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_home_location);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMapView = (MapView)findViewById(R.id.bmap);
        baiduMap = mMapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if (homeMarker != null)
                    homeMarker.setPosition(point);
                else
                    homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions.position(point));
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(point));
                mCurrentLongitude = point.longitude;
                mCurrentLatitude = point.latitude;
            }

            @Override
            public boolean onMapPoiClick(MapPoi arg0) {
                return false;
            }
        });
        homeMarkerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(ImageUtil.getBitmapFromDrawable(this, R.drawable.ic_location_red)))
                .draggable(false);


        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        //Show origin home location
        mCurrentLongitude = getIntent().getDoubleExtra(BUNDLE_HOME_LONGITUDE, -1);
        mCurrentLatitude = getIntent().getDoubleExtra(BUNDLE_HOME_LATITUDE, -1);
        if(mCurrentLongitude >= 0 && mCurrentLatitude >= 0){
            LatLng point = new LatLng(mCurrentLatitude, mCurrentLongitude);
            homeMarkerOptions.position(point);
            homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions);
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
            editAddr.setText(getIntent().getStringExtra(BUNDLE_HOME_ADDR));
        }
    }

    @OnClick(R.id.btn_save)
    void onBtnSaveClick(View v){
        saveHomeLocation();
    }

    private void saveHomeLocation() {
        if(TextUtils.isEmpty(editAddr.getText().toString())){
            editAddr.setError(getString(R.string.error_homeaddress_empty));
            return;
        }

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage(getString(R.string.action_save_home_location));
        pd.show();

        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.OlderAccountAPI.SET_HOME_LOCATION), Request.Method.POST, GlobalInfo.token,
                new SaveHomeLocationRequest(mCurrentLongitude, mCurrentLatitude, editAddr.getText().toString()),
                new HttpApiJsonListener<Result>(Result.class) {
                    @Override
                    public void onResponse() {
                        pd.dismiss();
                    }

                    @Override
                    public void onDataResponse(Result data) {
                        Toast.makeText(SettingHomeLocationActivity.this, data.getMessage(), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CODE_SET_HOME_LOCATION);
                        finish();
                    }
                }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_set_home_location, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query == null || query.isEmpty())
                    return true;
                mSearch.geocode(new GeoCodeOption()
                        .city("")
                        .address(query));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        mSearch.destroy();
        super.onDestroy();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            if(geoCodeResult != null)
                Log.e(GlobalInfo.LOG_TAG, "Geo error:" + geoCodeResult.error);
            Toast.makeText(this, R.string.error_address_notfind, Toast.LENGTH_SHORT)
                    .show();
        }
        else{
            homeMarkerOptions.position(geoCodeResult.getLocation());
            if(homeMarker != null)
                homeMarker.remove();
            homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions);
            baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(geoCodeResult
                    .getLocation()));
            mCurrentLongitude = geoCodeResult.getLocation().longitude;
            mCurrentLatitude = geoCodeResult.getLocation().latitude;
            editAddr.setText(geoCodeResult.getAddress());
        }
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            if(reverseGeoCodeResult != null)
                Log.e(GlobalInfo.LOG_TAG, "Geo reverse error:" + reverseGeoCodeResult.error);
        } else {
            editAddr.setText(reverseGeoCodeResult.getAddress());
        }
    }

}
