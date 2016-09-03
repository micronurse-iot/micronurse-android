package org.micronurse;

import android.util.Log;

import com.baidu.mapapi.SDKInitializer;

import org.micronurse.util.GlobalInfo;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //Init JPush Service
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);

        //Init Baidu Map SDK
        SDKInitializer.initialize(this);
    }
}
