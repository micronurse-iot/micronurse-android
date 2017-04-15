package org.micronurse.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhou-shengyun on 8/15/16.
 */
public class Sensor implements Serializable {
    public static final String SENSOR_TYPE_THERMOMETER = "thermometer";
    public static final String SENSOR_TYPE_HUMIDOMETER = "humidometer";
    public static final String SENSOR_TYPE_INFRARED_TRANSDUCER = "infrared_transducer";
    public static final String SENSOR_TYPE_SMOKE_TRANSDUCER = "smoke_transducer";
    public static final String SENSOR_TYPE_GPS = "gps";
    public static final String SENSOR_TYPE_FEVER_THERMOMETER = "fever_thermometer";
    public static final String SENSOR_TYPE_PULSE_TRANSDUCER = "pulse_transducer";

    private Date timestamp;

    public Sensor(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
