package org.micronurse.model;

/**
 * Created by zhou-shengyun on 8/18/16.
 */
public class SmokeTransducer extends Sensor {
    private String name;
    private Integer smoke;

    public SmokeTransducer(long timestamp, String name, Integer smoke) {
        super(timestamp);
        this.name = name;
        this.smoke = smoke;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSmoke() {
        return smoke;
    }

    public void setSmoke(Integer smoke) {
        this.smoke = smoke;
    }
}
