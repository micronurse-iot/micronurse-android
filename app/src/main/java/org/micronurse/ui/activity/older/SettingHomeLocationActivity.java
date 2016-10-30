package org.micronurse.ui.activity.older;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import org.micronurse.http.APIErrorListener;
import org.micronurse.http.MicronurseAPI;
import org.micronurse.http.model.PublicResultCode;
import org.micronurse.http.model.request.SaveHomeLocationRequest;
import org.micronurse.http.model.result.HomeLocationResult;
import org.micronurse.http.model.result.Result;
import org.micronurse.model.User;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ImageUtil;

public class SettingHomeLocationActivity extends AppCompatActivity  implements
        OnGetGeoCoderResultListener {
    public static final int RESULT_CODE_SET_HOME_LOCATION = 666;
    public static final String BUNDLE_HOME_LONGITUDE = "HomeLongitude";
    public static final String BUNDLE_HOME_LATITUDE = "HomeLatitude";

    private EditText txtCity;
    private EditText txtAddr;
    private Button btnSearchLocation;
    private Button btnSave;

    private MapView mMapView;
    private BaiduMap baiduMap;
    private MarkerOptions homeMarkerOptions;
    private Marker homeMarker;
    private GeoCoder mSearch;
    private String cityName;
    private String addrDetail;
    private Double mCurrentLatitude, mCurrentLongitude;


    //private SensorDataReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_home_location);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtCity = (EditText)findViewById(R.id.txt_city);
        txtCity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                txtCity.setError(null);
                if (TextUtils.isEmpty(txtCity.getText())) {
                    txtCity.setError(getString(R.string.error_cityname_empty));
                    txtCity.requestFocus();
                    return false;
                } else {
                    txtAddr.requestFocus();
                }
                return true;
            }
        });
        txtAddr = (EditText)findViewById(R.id.txt_home_location);
        txtAddr.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                searchLocation();
                return true;
            }
        });
        btnSearchLocation = (Button)findViewById(R.id.btn_search_location);
        btnSearchLocation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchLocation();
            }
        });
        btnSave = (Button) findViewById(R.id.btn_save_home_location);
        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHomeLocation();
            }
        });

        mMapView = (MapView)findViewById(R.id.bmapView);
        baiduMap = mMapView.getMap();
        baiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(17));
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                homeMarkerOptions.position(point);
                if (homeMarker != null)
                    homeMarker.remove();
                homeMarker = (Marker) baiduMap.addOverlay(homeMarkerOptions);
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
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(new LatLng(mCurrentLatitude, mCurrentLongitude)));
        }
    }


    private boolean searchLocation() {
        txtCity.setError(null);
        txtAddr.setError(null);
        if (TextUtils.isEmpty(txtCity.getText())) {
            txtCity.setError(getString(R.string.error_cityname_empty));
            txtCity.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(txtAddr.getText())) {
            txtAddr.setError(getString(R.string.error_homeaddress_empty));
            txtAddr.requestFocus();
            return false;
        } else {
            cityName = txtCity.getText().toString();
            addrDetail = txtAddr.getText().toString();
            mSearch.geocode(new GeoCodeOption()
                    .city(cityName)
                    .address(cityName+addrDetail));

        }
        return true;
    }

    private void saveHomeLocation() {
        if (searchLocation()) {
            new MicronurseAPI<Result>(this, MicronurseAPI.getApiUrl(MicronurseAPI.OlderAccountAPI.SET_HOME_LOCATION), Request.Method.POST, new SaveHomeLocationRequest(
                    mCurrentLongitude, mCurrentLatitude
            ), GlobalInfo.token, new Response.Listener<Result>() {
                @Override
                public void onResponse(Result response) {
                    setResult(RESULT_CODE_SET_HOME_LOCATION);
                    finish();
                }
            }, new APIErrorListener() {
                @Override
                public boolean onErrorResponse(VolleyError err, Result result) {
                    if (result != null) {
                        switch (result.getResultCode()) {
                            case PublicResultCode.HOME_ADDRESS_NOT_EXIST:
                                txtCity.setError(result.getMessage());
                                txtAddr.setHint(R.string.error_address_notfind);
                                txtCity.requestFocus();
                                return true;
                        }
                    }
                    return false;
                }
            }, Result.class, true, getString(R.string.action_save_home_location)).startRequest();
        }
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
            Log.e(GlobalInfo.LOG_TAG, "Geo error:" + geoCodeResult.error);
            Toast.makeText(this, R.string.error_address_notfind, Toast.LENGTH_LONG)
                    .show();
            return;
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
        }
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(GlobalInfo.LOG_TAG, "Geo reverse error:" + reverseGeoCodeResult.error);
            Toast.makeText(this, R.string.unknown_location, Toast.LENGTH_LONG)
                    .show();
        } else {
            ReverseGeoCodeResult.AddressComponent address = reverseGeoCodeResult.getAddressDetail();
            txtCity.setText(address.city);
            txtAddr.setText(address.district + address.street + address.streetNumber);
        }
    }

}
