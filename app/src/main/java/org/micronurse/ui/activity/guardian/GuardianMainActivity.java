package org.micronurse.ui.activity.guardian;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.micronurse.R;
import org.micronurse.database.model.Guardianship;
import org.micronurse.model.User;
import org.micronurse.ui.activity.guardian.main.ContactsFragment;
import org.micronurse.ui.activity.guardian.main.MonitorFragment;
import org.micronurse.ui.activity.guardian.main.MonitorWarningFragment;
import org.micronurse.util.DatabaseUtil;
import org.micronurse.util.GlobalInfo;
import de.hdodenhof.circleimageview.CircleImageView;

public class GuardianMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView mNavigationView;
    private View mNavHeaderView;
    private FragmentManager mFragmentManager;
    private MonitorFragment monitorFragment;
    private MonitorWarningFragment monitorWarningFragment;
    private ContactsFragment contactsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardian_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_guardian_main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_older_main);
        mNavHeaderView = mNavigationView.getHeaderView(0);

        monitorFragment = new MonitorFragment();
        monitorWarningFragment = new MonitorWarningFragment();
        contactsFragment = new ContactsFragment();
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .add(R.id.guardian_main_container, monitorFragment)
                .add(R.id.guardian_main_container, monitorWarningFragment)
                .add(R.id.guardian_main_container, contactsFragment)
                .commit();

        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(mNavigationView.getMenu().getItem(0));
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
        ((CircleImageView)mNavHeaderView.findViewById(R.id.nav_header_older_portrait)).setImageBitmap(GlobalInfo.Guardian.monitorOlder.getPortrait());
    }

    @Override
    protected void onResume() {
        super.onResume();
        DatabaseUtil.updateLoginRecord(GlobalInfo.user, GlobalInfo.token);
        updateMonitorOlder();

        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_nickname)).setText(GlobalInfo.user.getNickname());
        ((TextView)mNavHeaderView.findViewById(R.id.nav_header_phone_num)).setText(GlobalInfo.user.getPhoneNumber());
        if(GlobalInfo.user.getPortrait() != null)
            ((CircleImageView)mNavHeaderView.findViewById(R.id.nav_header_portrait)).setImageBitmap(GlobalInfo.user.getPortrait());
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        FragmentTransaction t;
        switch (id){
            case R.id.guardian_nav_monitor:
                setTitle(R.string.action_monitor);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(monitorFragment);
                t.commit();
                break;
            case R.id.guardian_nav_monitor_warning:
                setTitle(R.string.action_monitor_warning);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(monitorWarningFragment);
                t.commit();
                break;
            case R.id.guardian_nav_contacts:
                setTitle(R.string.action_contacts_older);
                t = mFragmentManager.beginTransaction();
                hideAllFragment(t);
                t.show(contactsFragment);
                t.commit();
                break;
            case R.id.guardian_nav_exit:
                //TODO:do something before exit
                finish();
                break;
            case R.id.guardian_nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    private void hideAllFragment(FragmentTransaction ft){
        ft.hide(monitorFragment);
        ft.hide(monitorWarningFragment);
        ft.hide(contactsFragment);
    }
}
