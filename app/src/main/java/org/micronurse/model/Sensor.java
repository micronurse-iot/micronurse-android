package org.micronurse.model;

/**
 * Created by zhou-shengyun on 8/15/16.
 */
public class Sensor {
    private long timestamp;

    public Sensor(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
