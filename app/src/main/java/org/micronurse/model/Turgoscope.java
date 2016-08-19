package org.micronurse.model;

/**
 * Created by zhou-shengyun on 8/19/16.
 */
public class Turgoscope extends Sensor {
    private Integer lowBloodPressure;
    private Integer highBloodPressure;

    public Turgoscope(long timestamp, Integer lowBloodPressure, Integer highBloodPressure) {
        super(timestamp);
        this.lowBloodPressure = lowBloodPressure;
        this.highBloodPressure = highBloodPressure;
    }


    public Integer getLowBloodPressure() {
        return lowBloodPressure;
    }

    public void setLowBloodPressure(Integer lowBloodPressure) {
        this.lowBloodPressure = lowBloodPressure;
    }

    public Integer getHighBloodPressure() {
        return highBloodPressure;
    }

    public void setHighBloodPressure(Integer highBloodPressure) {
        this.highBloodPressure = highBloodPressure;
    }
}
