package org.micronurse.model;

/**
 * Created by shengyun-zhou <GGGZ-1101-28@Live.cn> on 2017-05-15
 */
public class SensorConfig {
    private Boolean infraredEnabled;

    public SensorConfig(Boolean infraredEnabled) {
        this.infraredEnabled = infraredEnabled;
    }

    public Boolean isInfraredSwitch() {
        return infraredEnabled;
    }

    public void setInfraredEnabled(Boolean infraredEnabled) {
        this.infraredEnabled = infraredEnabled;
    }
}
