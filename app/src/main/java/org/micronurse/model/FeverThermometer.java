package org.micronurse.model;

import java.util.Date;

/**
 * Created by zhou-shengyun on 8/19/16.
 */
public class FeverThermometer extends Sensor {
    private Float temperature;

    public FeverThermometer(Date timestamp, Float temperature) {
        super(timestamp);
        this.temperature = temperature;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }
}
