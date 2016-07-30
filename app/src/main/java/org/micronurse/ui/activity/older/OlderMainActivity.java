package org.micronurse.ui.activity.older;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.activeandroid.query.Select;

import org.micronurse.R;
import org.micronurse.database.model.LoginUserRecord;
import org.micronurse.util.GlobalInfo;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class OlderMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private NavigationView mNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_older_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_older_main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.getMenu().getItem(0).setChecked(true);
        setTitle(R.string.action_monitor);

        GlobalInfo.loginRecord = new Select().from(LoginUserRecord.class)
                                .where("PhoneNumber=?", GlobalInfo.user.getPhoneNumber())
                                .executeSingle();
        if(GlobalInfo.loginRecord == null){
            GlobalInfo.loginRecord = new LoginUserRecord(GlobalInfo.user.getPhoneNumber(), GlobalInfo.token, GlobalInfo.user.getPortrait());
            GlobalInfo.loginRecord.save();
        }else{
            GlobalInfo.loginRecord.setLastLoginTime(new Date());
            GlobalInfo.loginRecord.setPortrait(GlobalInfo.user.getPortrait());
            GlobalInfo.loginRecord.setToken(GlobalInfo.token);
            GlobalInfo.loginRecord.save();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((TextView)mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_nickname_older_main)).setText(GlobalInfo.user.getNickname());
        ((TextView)mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_phone_num_older_main)).setText(GlobalInfo.user.getPhoneNumber());
        if(GlobalInfo.user.getPortrait() != null)
            ((CircleImageView)mNavigationView.getHeaderView(0).findViewById(R.id.nav_header_portrait_older_main)).setImageBitmap(GlobalInfo.user.getPortrait());
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
        switch (id){
            case R.id.older_nav_exit:
                finish();
                break;
        }
        return true;
    }
}
