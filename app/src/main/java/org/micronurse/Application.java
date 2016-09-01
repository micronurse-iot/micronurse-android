package org.micronurse;

import com.baidu.mapapi.SDKInitializer;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
        SDKInitializer.initialize(this);
    }
}
