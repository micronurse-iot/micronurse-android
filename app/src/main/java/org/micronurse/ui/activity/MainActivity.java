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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.micronurse.Application;
import org.micronurse.R;
import org.micronurse.model.User;
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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_main)
    NavigationView mNavigationView;
    private SwitchCompat mSwitchEmergencyCall;
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
                        .getActionView().findViewById(R.id.switch_emergency_call_btn);
                mSwitchEmergencyCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        emergencyCallService.setShowCallButton(isChecked);
                    }
                });

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
        ((ImageView)mNavHeaderView.findViewById(R.id.nav_header_older_portrait)).setImageBitmap(GlobalInfo.Guardian.monitorOlder.getPortrait());
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
            case R.id.nav_contacts:
                setTitle(R.string.action_contacts_older);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(contactsFragment);
                t.commit();
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
}
