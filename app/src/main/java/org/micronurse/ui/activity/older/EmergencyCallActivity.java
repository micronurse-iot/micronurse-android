package org.micronurse.ui.activity.older;

/**
 * Created by Lsq on 2016/10/25.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.List;

import org.micronurse.R;
import org.micronurse.adapter.GuardianListAdapter;
import org.micronurse.model.User;
import org.micronurse.util.GlobalInfo;
import android.os.CountDownTimer;
import android.widget.Toast;

public class EmergencyCallActivity extends AppCompatActivity  {
    private static final int COUNTDOWN_SECOND = 10;

    private RecyclerView guardianListsView;
    private TextView txtCountdown;
    private Countdown countdown;
    private GuardianListAdapter guardianListAdapter;
    private List<User> dataList = GlobalInfo.guardianshipList;


    public EmergencyCallActivity(){
        //Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_call);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtCountdown = (TextView)findViewById(R.id.countdown);
        countdown = new Countdown(COUNTDOWN_SECOND * 1000, 1000);
        guardianListsView = (RecyclerView)findViewById(R.id.guardians_lists);
        guardianListsView.setLayoutManager(new LinearLayoutManager(this));
        guardianListAdapter = new GuardianListAdapter(EmergencyCallActivity.this, dataList);
        guardianListAdapter.setOnItemClickListener(new GuardianListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User guardian) {
                callGuardian(guardian);
            }
        });
        guardianListsView.setAdapter(guardianListAdapter);
        countdown.start();
    }

    private void callGuardian(User guardian){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + guardian.getPhoneNumber()));
        startActivity(intent);
        finish();
    }

    private class Countdown extends CountDownTimer{
        public Countdown(long millisFuture, long countdownInterval){
            super(millisFuture,countdownInterval);
        }

        @Override
        public void onFinish(){
            callGuardian(dataList.get(0));
        }

        @Override
        public void onTick(long millisUtilFinished){
            if(dataList != null && !isFinishing()){
                txtCountdown.setText(String.format(getString(R.string.call_after_n_seconds),
                                     millisUtilFinished / 1000));
            }
        }
    }

    private void cancelCountdown(){
        if(countdown != null){
            countdown.cancel();
            Toast toast =  Toast. makeText(EmergencyCallActivity.this, R.string.stopped_emergency_call, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            countdown = null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            cancelCountdown();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        cancelCountdown();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(countdown != null)
            countdown.cancel();
        super.onDestroy();
    }
}

