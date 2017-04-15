package org.micronurse.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhou-shengyun on 16-10-3.
 */

public class RawSensorData implements Serializable {
    private String sensorType;
    private Date timestamp;
    private String name;
    private String value;

    public RawSensorData(String sensorType, Date timestamp, String value) {
        this(sensorType, timestamp, null, value);
    }

    public RawSensorData(String sensorType, Date timestamp, String name, String value) {
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
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
