package org.micronurse.ui.activity;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.database.model.Guardianship;
import org.micronurse.model.User;
import org.micronurse.service.EmergencyCallService;
import org.micronurse.service.LocationService;
import org.micronurse.service.MQTTService;
import org.micronurse.ui.activity.older.SettingsActivity;
import org.micronurse.ui.fragment.guardian.ContactsFragment;
import org.micronurse.ui.fragment.older.FriendJuanFragment;
import org.micronurse.ui.fragment.older.MedicationReminderFragment;
import org.micronurse.ui.fragment.MonitorFragment;
import org.micronurse.ui.fragment.MonitorWarningFragment;
import org.micronurse.ui.listener.OnFullScreenListener;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView mNavigationView;
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
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
            setContentView(R.layout.activity_older_main);
        else
            setContentView(R.layout.activity_guardian_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mFragmentManager = getSupportFragmentManager();
        monitorFragment = MonitorFragment.getInstance(this);
        monitorWarningFragment = MonitorWarningFragment.getInstance(this);

        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            friendJuanFragment = FriendJuanFragment.getInstance(this);
            medicationReminderFragment = new MedicationReminderFragment();
            mFragmentManager.beginTransaction()
                    .add(R.id.main_container, monitorFragment)
                    .add(R.id.main_container, monitorWarningFragment)
                    .add(R.id.main_container, friendJuanFragment)
                    .add(R.id.main_container, medicationReminderFragment)
                    .commit();
        }else{
            contactsFragment = ContactsFragment.getInstance(this);
            mFragmentManager.beginTransaction()
                    .add(R.id.main_container, monitorFragment)
                    .add(R.id.main_container, monitorWarningFragment)
                    .add(R.id.main_container, contactsFragment)
                    .commit();
        }

        mNavigationView = (NavigationView) findViewById(R.id.nav_older_main);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        mNavHeaderView = mNavigationView.getHeaderView(0);
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

        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_GUARDIAN){
            updateMonitorOlder();
        }

        mqttServiceIntent = new Intent(this, MQTTService.class);
        startService(mqttServiceIntent);
        mqttServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MQTTService mqttService = ((MQTTService.MQTTServiceBinder) service).getService();
                monitorFragment.onBind(mqttService);
                monitorWarningFragment.onBind(mqttService);
                if(friendJuanFragment != null)
                    friendJuanFragment.onBind(mqttService);
                if(contactsFragment != null)
                    contactsFragment.onBind(mqttService);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(mqttServiceIntent, mqttServiceConnection, Context.BIND_AUTO_CREATE);
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
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
            locationServiceIntent = new Intent(this, LocationService.class);
            startService(locationServiceIntent);
        }
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
        }
        if(GlobalInfo.Guardian.monitorOlder == null){
            Guardianship guardianship = DatabaseUtil.findDefaultMonitorOlder(GlobalInfo.user.getPhoneNumber());
            if(guardianship == null){
                GlobalInfo.Guardian.monitorOlder = GlobalInfo.guardianshipList.get(0);
                guardianship = new Guardianship(GlobalInfo.user.getPhoneNumber(), GlobalInfo.Guardian.monitorOlder.getPhoneNumber());
                guardianship.save();
            }else{
                for(User u: GlobalInfo.guardianshipList){
                    if(u.getPhoneNumber().equals(guardianship.getOlderId())){
                        GlobalInfo.Guardian.monitorOlder = u;
                        break;
                    }
                }
                if(GlobalInfo.Guardian.monitorOlder == null){
                    GlobalInfo.Guardian.monitorOlder = GlobalInfo.guardianshipList.get(0);
                    guardianship.setOlderId(GlobalInfo.Guardian.monitorOlder.getPhoneNumber());
                    guardianship.save();
                }
            }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER) {
            getMenuInflater().inflate(R.menu.activity_older_main_menu, menu);
            MenuItem item = menu.findItem(R.id.menu_switch_call_btn);
            SwitchCompat switchBtn = (SwitchCompat) item.getActionView().findViewById(R.id.switch_emergency_call_btn);
            switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    emergencyCallService.setShowCallButton(isChecked);
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
                                //TODO:do something before exit
                                if(locationServiceIntent != null)
                                    stopService(locationServiceIntent);
                                if(emergencyCallServiceIntent != null)
                                    stopService(emergencyCallServiceIntent);
                                stopService(mqttServiceIntent);
                                finish();
                            }
                        }).setNegativeButton(R.string.action_cancel, null).create();
                ad.show();
                break;
            case R.id.nav_settings:
                Intent intent;
                if(GlobalInfo.user.getAccountType() == User.ACCOUNT_TYPE_OLDER)
                    intent = new Intent(this, SettingsActivity.class);
                else
                    intent = new Intent(this, org.micronurse.ui.activity.guardian.SettingsActivity.class);
                startActivity(intent);
                break;
        }
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
