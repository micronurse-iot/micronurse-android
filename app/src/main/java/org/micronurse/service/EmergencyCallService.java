package org.micronurse.service;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.micronurse.R;
import org.micronurse.ui.activity.older.EmergencyCallActivity;
import org.micronurse.util.GlobalInfo;

/**
 * Created by zhou-shengyun on 8/20/16.
 */
public class EmergencyCallService extends Service {
    private View floatView;
    private FloatingActionButton callButton;
    private WindowManager.LayoutParams wmParams;
    private WindowManager windowManager;
    private Intent intent;
    private int btnStartX;
    private int btnStartY;
    private int startX;
    private int startY;
    private int maxDeltaX;
    private int maxDeltaY;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new EmergencyCallServiceBinder();
    }

    private void createFloatView(){
        wmParams = new WindowManager.LayoutParams();
        windowManager = (WindowManager)getApplication().getSystemService(Application.WINDOW_SERVICE);
        wmParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.format = PixelFormat.RGBA_8888;    //transparent background
        wmParams.gravity = Gravity.START | Gravity.TOP;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setTheme(R.style.AppTheme_NoActionBar);
        floatView = LayoutInflater.from(this).inflate(R.layout.emergency_call_btn, null);
        windowManager.addView(floatView, wmParams);
        callButton = (FloatingActionButton)floatView.findViewById(R.id.btn_emergency_call);
        callButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        btnStartX = wmParams.x;
                        btnStartY = wmParams.y;
                        startX = (int)motionEvent.getRawX();
                        startY = (int)motionEvent.getRawY();
                        maxDeltaX = maxDeltaY = 0;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = startX - (int)motionEvent.getRawX();
                        int deltaY = startY - (int)motionEvent.getRawY();
                        if(Math.abs(deltaX) > maxDeltaX)
                            maxDeltaX = Math.abs(deltaX);
                        if(Math.abs(deltaY) > maxDeltaY)
                            maxDeltaY = Math.abs(deltaY);
                        wmParams.x = btnStartX - deltaX;
                        wmParams.y = btnStartY - deltaY;
                        windowManager.updateViewLayout(floatView, wmParams);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(maxDeltaX <= 5 && maxDeltaY <= 5){
                            callButton.performClick();
                        }
                        return true;
                }
                return false;
            }
        });
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                intent = new Intent(EmergencyCallService.this, EmergencyCallActivity.class);
                startActivity(intent);
                Log.i(GlobalInfo.LOG_TAG, "Emergency call button clicked.");
            }
        });
    }

    @Override
    public void onDestroy() {
        windowManager.removeViewImmediate(floatView);
        super.onDestroy();
    }

    public void setShowCallButton(boolean show){
        if(!show)
            floatView.setVisibility(View.GONE);
        else
            floatView.setVisibility(View.VISIBLE);
    }

    public class EmergencyCallServiceBinder extends Binder {
        public EmergencyCallService getService(){
            return EmergencyCallService.this;
        }
    }
}
