package org.micronurse.service;

import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.micronurse.R;

/**
 * Created by zhou-shengyun on 8/20/16.
 */
public class EmergencyCallService extends Service {
    private View floatView;
    private FloatingActionButton callButton;
    private WindowManager.LayoutParams wmParams;
    private WindowManager windowManager;
    private int btnStartX;
    private int btnStartY;
    private int startX;
    private int startY;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int deltaX = startX - (int)motionEvent.getRawX();
                        int deltaY = startY - (int)motionEvent.getRawY();
                        wmParams.x = btnStartX - deltaX;
                        wmParams.y = btnStartY - deltaY;
                        windowManager.updateViewLayout(floatView, wmParams);
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public void onDestroy() {
        windowManager.removeViewImmediate(floatView);
        super.onDestroy();
    }
}
