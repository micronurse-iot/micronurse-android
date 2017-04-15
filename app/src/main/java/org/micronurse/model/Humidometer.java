package org.micronurse.model;

import java.util.Date;

/**
 * Created by zhou-shengyun on 8/18/16.
 */
public class Humidometer extends Sensor {
    private String name;
    private Float humidity;

    public Humidometer(Date timestamp, String name, Float humidity) {
        super(timestamp);
        this.name = name;
        this.humidity = humidity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }
}
