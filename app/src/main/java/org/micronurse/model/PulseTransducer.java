package org.micronurse.model;

/**
 * Created by zhou-shengyun on 8/19/16.
 */
public class PulseTransducer extends Sensor {
    private Integer pulse;

    public PulseTransducer(long timestamp, Integer pulse) {
        super(timestamp);
        this.pulse = pulse;
    }

    public Integer getPulse() {
        return pulse;
    }

    public void setPulse(Integer pulse) {
        this.pulse = pulse;
    }
}
