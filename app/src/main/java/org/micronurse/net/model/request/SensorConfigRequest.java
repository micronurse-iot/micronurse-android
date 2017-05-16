package org.micronurse.net.model.request;

import org.micronurse.model.SensorConfig;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-05-15
 */
public class SensorConfigRequest {
    private SensorConfig config;

    public SensorConfigRequest(SensorConfig config) {
        this.config = config;
    }

    public SensorConfig getConfig() {
        return config;
    }

    public void setConfig(SensorConfig config) {
        this.config = config;
    }
}
