package org.micronurse.ui.activity.older;

/**
 * Created by Lsq on 2016/10/25.
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.micronurse.R;
import org.micronurse.adapter.GuardianListsAdapter;
import org.micronurse.model.User;
import org.micronurse.util.GlobalInfo;
import android.os.CountDownTimer;
import android.widget.Toast;

public class EmergencyCallActivity extends AppCompatActivity  {
    private View view;
    private RecyclerView guardianListsView;
    private TextView txtCountdown;
    private Countdown countdown;
    private GuardianListsAdapter guardianListsAdapter;
    private List<User> dataList = GlobalInfo.guardianshipList;


    public EmergencyCallActivity(){
        //Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergency_call);
        txtCountdown = (TextView)findViewById(R.id.countdown);
        countdown = new Countdown(30000,1000);
        guardianListsView = (RecyclerView)findViewById(R.id.guardians_lists);
        guardianListsView.setLayoutManager(new LinearLayoutManager(this));
        guardianListsAdapter = new GuardianListsAdapter(EmergencyCallActivity.this,dataList);
        guardianListsView.setAdapter(guardianListsAdapter);
        countdown.start();
    }


    class Countdown extends CountDownTimer{
        public Countdown(long millisFuture,long countdownInterval){
            super(millisFuture,countdownInterval);
        }

        @Override
        public void onFinish(){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + dataList.get(0).getPhoneNumber()));
            startActivity(intent);
        }

        @Override
        public void onTick(long millisUtilFinished){
            if(dataList != null && isFinishing() == false){
                txtCountdown.setText(millisUtilFinished / 1000 + "秒后将自动拨号");
            }
           else{
                mHandler.sendEmptyMessage(0);
                txtCountdown.setText(millisUtilFinished / 1000 + "秒");
                Toast toast =  Toast. makeText(EmergencyCallActivity.this, "停止一键呼救",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        }
    }

    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            if (countdown!=null) {
                countdown.cancel();
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}

