package org.micronurse.net.model.result;

import org.micronurse.model.SensorConfig;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-05-15
 */
public class SensorConfigResult extends Result {
    private SensorConfig config;

    public SensorConfigResult(int resultCode, String message, SensorConfig config) {
        super(resultCode, message);
        this.config = config;
    }

    public SensorConfig getConfig() {
        return config;
    }
}
