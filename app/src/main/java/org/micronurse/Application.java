package org.micronurse;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by zhou-shengyun on 8/31/16.
 */
public class Application extends com.activeandroid.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
