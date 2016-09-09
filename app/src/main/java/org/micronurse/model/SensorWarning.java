package org.micronurse.model;

/**
 * Created by zhou-shengyun on 9/8/16.
 */
public class SensorWarning {
    private String sensorType;
    private Sensor sensorData;

    public SensorWarning(String sensorType, Sensor sensorData) {
        this.sensorType = sensorType;
        this.sensorData = sensorData;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public Sensor getSensorData() {
        return sensorData;
    }

    public void setSensorData(Sensor sensorData) {
        this.sensorData = sensorData;
    }
}
