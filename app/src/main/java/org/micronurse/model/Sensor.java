package org.micronurse.model;

/**
 * Created by zhou-shengyun on 8/15/16.
 */
public class Sensor {
    public static final String SENSOR_TYPE_THERMOMETER = "thermometer";
    public static final String SENSOR_TYPE_HUMIDOMETER = "humidometer";
    public static final String SENSOR_TYPE_INFRARED_TRANSDUCER = "infrared_transducer";
    public static final String SENSOR_TYPE_SMOKE_TRANSDUCER = "smoke_transducer";
    public static final String SENSOR_TYPE_GPS = "gps";
    public static final String SENSOR_TYPE_FEVER_THERMOMETER = "fever_thermometer";
    public static final String SENSOR_TYPE_PULSE_TRANSDUCER = "pulse_transducer";
    public static final String SENSOR_TYPE_TURGOSCOPE = "turgoscope";

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
