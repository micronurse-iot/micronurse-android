package org.micronurse.model;

/**
 * Created by zhou-shengyun on 8/15/16.
 */
public class Thermometer extends Sensor {
    private String name;
    private Float temperature;

    public Thermometer(long timestamp, String name, float temperature) {
        super(timestamp);
        this.name = name;
        this.temperature = temperature;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }
}
