package org.micronurse.ui.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.model.SensorConfig;
import org.micronurse.model.User;
import org.micronurse.net.http.HttpApi;
import org.micronurse.net.http.HttpApiJsonListener;
import org.micronurse.net.http.HttpApiJsonRequest;
import org.micronurse.net.model.request.SensorConfigRequest;
import org.micronurse.net.model.result.Result;
import org.micronurse.net.model.result.SensorConfigResult;
import org.micronurse.service.EmergencyCallService;
import org.micronurse.service.LocationService;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.fragment.guardian.ContactsFragment;
import org.micronurse.ui.fragment.older.FriendJuanFragment;
import org.micronurse.ui.fragment.older.MedicationReminderFragment;
import org.micronurse.ui.fragment.MonitorFragment;
import org.micronurse.ui.fragment.MonitorWarningFragment;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.HttpAPIUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_main)
    NavigationView mNavigationView;
    private MenuItem menuItemSwitchInfrared;
    private SwitchCompat mSwitchEmergencyCall;
    private SwitchCompat mSwitchInfrared;
    private CompoundButton.OnCheckedChangeListener onInfraredChangedListener;
    private View mNavHeaderView;

    private FragmentManager mFragmentManager;
    private MonitorFragment monitorFragment;
    private MonitorWarningFragment monitorWarningFragment;

    private FriendJuanFragment friendJuanFragment;
    private MedicationReminderFragment medicationReminderFragment;

    private ContactsFragment contactsFragment;
    private Intent mqttServiceIntent;
    private ServiceConnection mqttServiceConnection;
    private Intent locationServiceIntent;
    private Intent emergencyCallServiceIntent;
    private EmergencyCallService emergencyCallService;
    private ServiceConnection emergencyCallServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ArrayList<String> permToCheck = new ArrayList<>();
        permToCheck.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        mFragmentManager = getSupportFragmentManager();
        monitorFragment = MonitorFragment.getInstance(this);
        monitorWarningFragment = MonitorWarningFragment.getInstance(this);
        FragmentTransaction ft = mFragmentManager.beginTransaction()
                .add(R.id.main_container, monitorFragment)
                .add(R.id.main_container, monitorWarningFragment);

        switch (GlobalInfo.user.getAccountType()){
            case User.ACCOUNT_TYPE_OLDER:
                friendJuanFragment = FriendJuanFragment.getInstance(this);
                medicationReminderFragment = MedicationReminderFragment.getInstance(this);
                ft.add(R.id.main_container, friendJuanFragment)
                  .add(R.id.main_container, medicationReminderFragment)
                  .commit();

                //Navigation
                mNavHeaderView = mNavigationView.inflateHeaderView(R.layout.nav_header_older_main);
                mNavigationView.inflateMenu(R.menu.activity_older_main_drawer);
                mSwitchEmergencyCall = (SwitchCompat) mNavigationView.getMenu().findItem(R.id.nav_switch_emergency_call)
                        .getActionView().findViewById(R.id.switch_menu_item);
                mSwitchEmergencyCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        emergencyCallService.setShowCallButton(isChecked);
                    }
                });

                menuItemSwitchInfrared = mNavigationView.getMenu().findItem(R.id.nav_switch_infrared_theft_proof);
                mSwitchInfrared = (SwitchCompat) menuItemSwitchInfrared.getActionView().findViewById(R.id.switch_menu_item);
                mSwitchInfrared.setChecked(false);
                mSwitchInfrared.setEnabled(false);
                menuItemSwitchInfrared.setEnabled(false);
                onInfraredChangedListener = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked)
                            changeInfraredConfig(true);
                        else{
                            AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                                    .setTitle(R.string.action_confirm)
                                    .setMessage(R.string.query_switch_infrared_off)
                                    .setCancelable(false)
                                    .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mSwitchInfrared.setOnCheckedChangeListener(null);
                                            mSwitchInfrared.setChecked(true);
                                            mSwitchInfrared.setOnCheckedChangeListener(onInfraredChangedListener);
                                        }
                                    })
                                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            changeInfraredConfig(false);
                                        }
                                    })
                                    .create();
                            ad.show();
                        }
                    }
                };

                HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.SensorAPI.SENSOR_CONFIG), Request.Method.GET, GlobalInfo.token, null,
                        new HttpApiJsonListener<SensorConfigResult>(SensorConfigResult.class) {
                            @Override
                            public void onResponse() {
                                mSwitchInfrared.setEnabled(true);
                                menuItemSwitchInfrared.setEnabled(true);
                                mSwitchInfrared.setOnCheckedChangeListener(onInfraredChangedListener);
                            }

                            @Override
                            public void onDataResponse(SensorConfigResult data) {
                                if(data.getConfig().isInfraredSwitch()){
                                    mSwitchInfrared.setOnCheckedChangeListener(null);
                                    mSwitchInfrared.setChecked(true);
                                    mSwitchInfrared.setOnCheckedChangeListener(onInfraredChangedListener);
                                }
                            }
                        }));


                //Services
                permToCheck.add(Manifest.permission.CALL_PHONE);
                emergencyCallServiceIntent = new Intent(this, EmergencyCallService.class);
                startService(emergencyCallServiceIntent);
                emergencyCallServiceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        emergencyCallService = ((EmergencyCallService.EmergencyCallServiceBinder)service).getService();
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {}
                };
                bindService(emergencyCallServiceIntent, emergencyCallServiceConnection, Context.BIND_AUTO_CREATE);
                permToCheck.add(Manifest.permission.ACCESS_COARSE_LOCATION);
                permToCheck.add(Manifest.permission.ACCESS_FINE_LOCATION);
                locationServiceIntent = new Intent(this, LocationService.class);
                startService(locationServiceIntent);
                break;
            case User.ACCOUNT_TYPE_GUARDIAN:
                if(GlobalInfo.guardianshipList != null && !GlobalInfo.guardianshipList.isEmpty())
                    GlobalInfo.Guardian.monitorOlder = GlobalInfo.guardianshipList.get(0);

                contactsFragment = ContactsFragment.getInstance(this);
                ft.add(R.id.main_container, contactsFragment)
                  .commit();

                //Navigation
                mNavHeaderView = mNavigationView.inflateHeaderView(R.layout.nav_header_guardian_main);
                mNavigationView.inflateMenu(R.menu.activity_guardian_main_drawer);
        }

        mNavigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(mNavigationView.getMenu().getItem(0));

        monitorFragment.setOnFullScreenListener(new OnFullScreenListener() {
            @Override
            public void onEnterFullScreen() {
                getSupportActionBar().hide();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onExitFullScreen() {
                getSupportActionBar().show();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });

        mqttServiceIntent = new Intent(this, MQTTService.class);
        startService(mqttServiceIntent);
        mqttServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MQTTService mqttService = ((MQTTService.Binder) service).getService();
                monitorFragment.onBind(mqttService);
                monitorWarningFragment.onBind(mqttService);
                if(friendJuanFragment != null)
                    friendJuanFragment.onBind(mqttService);
                if(contactsFragment != null)
                    contactsFragment.onBind(mqttService);
                try {
                    mqttService.startWork(GlobalInfo.user.getUserId(), GlobalInfo.token);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(mqttServiceIntent, mqttServiceConnection, Context.BIND_AUTO_CREATE);
        Application.checkPermission(this, permToCheck.toArray(new String[permToCheck.size()]));
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseUtil.updateLoginRecord(GlobalInfo.user, GlobalInfo.token);
        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_nickname)).setText(GlobalInfo.user.getNickname());
        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_phone_num)).setText(GlobalInfo.user.getPhoneNumber());
        if(GlobalInfo.user.getPortrait() != null)
            ((ImageView)mNavHeaderView.findViewById(R.id.nav_header_portrait)).setImageBitmap(GlobalInfo.user.getPortrait());
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN){
            updateMonitorOlder();
        }
    }

    private void updateMonitorOlder(){
        if(GlobalInfo.guardianshipList == null || GlobalInfo.guardianshipList.isEmpty()){
            mNavHeaderView.findViewById(R.id.nav_header_older_portrait).setVisibility(View.GONE);
            mNavHeaderView.findViewById(R.id.nav_header_older_nickname).setVisibility(View.GONE);
            return;
        }else{
            mNavHeaderView.findViewById(R.id.nav_header_older_portrait).setVisibility(View.VISIBLE);
            mNavHeaderView.findViewById(R.id.nav_header_older_nickname).setVisibility(View.VISIBLE);
        }
        if(GlobalInfo.Guardian.monitorOlder == null){
            GlobalInfo.Guardian.monitorOlder = GlobalInfo.guardianshipList.get(0);
        }
        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_older_nickname)).setText(GlobalInfo.Guardian.monitorOlder.getNickname());
        ImageView olderPortrait = ButterKnife.findById(mNavHeaderView, R.id.nav_header_older_portrait);
        olderPortrait.setImageBitmap(GlobalInfo.Guardian.monitorOlder.getPortrait());
        olderPortrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<CharSequence> choiceItems = new ArrayList<CharSequence>();
                int checked = -1;
                for(User u : GlobalInfo.guardianshipList) {
                    choiceItems.add(u.getNickname());
                    if(GlobalInfo.Guardian.monitorOlder != null && u.getUserId().equals(GlobalInfo.Guardian.monitorOlder.getUserId())){
                        checked = GlobalInfo.guardianshipList.indexOf(u);
                    }
                }
                if(choiceItems.isEmpty())
                    return;
                final AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(true)
                        .setTitle(R.string.action_switch_monitor_older)
                        .setSingleChoiceItems(choiceItems.toArray(new CharSequence[choiceItems.size()]), checked, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GlobalInfo.Guardian.monitorOlder = GlobalInfo.guardianshipList.get(which);
                                dialog.dismiss();
                                Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                                startActivity(refresh);
                                finish();
                            }
                        })
                        .create();
                ad.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ScanQRCodeActivity.REQUEST_CODE_SCAN_QR_CODE && resultCode == ScanQRCodeActivity.RESULT_CDOE_SCAN_QR_CODE){
            parseQRCode(data.getStringExtra(ScanQRCodeActivity.BUNDLE_QR_CODE_STR));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        if(emergencyCallServiceConnection != null)
            unbindService(emergencyCallServiceConnection);
        unbindService(mqttServiceConnection);
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        FragmentTransaction t;
        switch (id){
            case R.id.nav_monitor:
                setTitle(R.string.action_monitor);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(monitorFragment);
                t.commit();
                break;
            case R.id.nav_monitor_warning:
                setTitle(R.string.action_monitor_warning);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(monitorWarningFragment);
                t.commit();
                break;
            case R.id.nav_friend_juan:
                setTitle(R.string.action_friend_juan);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(friendJuanFragment);
                t.commit();
                break;
            case R.id.nav_medication_reminder:
                setTitle(R.string.action_medication_reminder);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(medicationReminderFragment);
                t.commit();
                break;
            case R.id.nav_switch_emergency_call:
                mSwitchEmergencyCall.setChecked(!mSwitchEmergencyCall.isChecked());
                return true;
            case R.id.nav_switch_infrared_theft_proof:
                mSwitchInfrared.setChecked(!mSwitchInfrared.isChecked());
                return true;
            case R.id.nav_contacts:
                setTitle(R.string.action_contacts_older);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(contactsFragment);
                t.commit();
                break;
            case R.id.nav_scan_qrcode:
                startActivityForResult(new Intent(this, ScanQRCodeActivity.class), ScanQRCodeActivity.REQUEST_CODE_SCAN_QR_CODE);
                break;
            case R.id.nav_exit:
                AlertDialog ad = new AlertDialog.Builder(this).setMessage(R.string.exit_query)
                        .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                GlobalInfo.exitLoginStatus(MainActivity.this);
                                finish();
                            }
                        }).setNegativeButton(R.string.action_cancel, null).create();
                ad.show();
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideAllFragment(FragmentTransaction ft){
        ft.hide(monitorFragment);
        ft.hide(monitorWarningFragment);
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            ft.hide(friendJuanFragment);
            ft.hide(medicationReminderFragment);
        }else {
            ft.hide(contactsFragment);
        }
    }

    private void parseQRCode(String s){
        if(s == null || s.isEmpty())
            return;
        try {
            JsonObject json = new JsonParser().parse(s).getAsJsonObject();
            if(json.get("action").getAsString().equals("iot_login") && GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER){
                HttpAPIUtil.loginIoT(this, json.get("token").getAsString());
            }
        } catch (JsonSyntaxException ignored){
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void changeInfraredConfig(final boolean on){
        mSwitchInfrared.setEnabled(false);
        menuItemSwitchInfrared.setEnabled(false);
        SensorConfig cfg = new SensorConfig(on);
        SensorConfigRequest req = new SensorConfigRequest(cfg);
        HttpApi.startRequest(new HttpApiJsonRequest(this, HttpApi.getApiUrl(HttpApi.SensorAPI.CHANGE_SENSOR_CONFIG), Request.Method.PUT, GlobalInfo.token,
                req, new HttpApiJsonListener<Result>(Result.class) {
            @Override
            public void onResponse() {
                mSwitchInfrared.setEnabled(true);
                menuItemSwitchInfrared.setEnabled(true);
            }

            @Override
            public void onDataResponse(Result data) {
                mSwitchInfrared.setOnCheckedChangeListener(null);
                mSwitchInfrared.setChecked(on);
                mSwitchInfrared.setOnCheckedChangeListener(onInfraredChangedListener);
            }

            @Override
            public void onErrorResponse() {
                mSwitchInfrared.setOnCheckedChangeListener(null);
                mSwitchInfrared.setChecked(!on);
                mSwitchInfrared.setOnCheckedChangeListener(onInfraredChangedListener);
            }
        }));
    }
}
