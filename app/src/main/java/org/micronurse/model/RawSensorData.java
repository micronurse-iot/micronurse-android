package org.micronurse.model;

import java.io.Serializable;

/**
 * Created by zhou-shengyun on 16-10-3.
 */

public class RawSensorData implements Serializable {
    private String sensorType;
    private Long timestamp;
    private String name;
    private String value;

    public RawSensorData(String sensorType, Long timestamp, String name, String value) {
        this.sensorType = sensorType;
        this.timestamp = timestamp;
        this.name = name;
        this.value = value;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
