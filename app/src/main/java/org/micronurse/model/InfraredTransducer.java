package org.micronurse.model;

import java.util.Date;

/**
 * Created by zhou-shengyun on 9/7/16.
 */
public class InfraredTransducer extends Sensor {
    private String name;
    private Boolean warning;

    public InfraredTransducer(Date timestamp, String name, Boolean warning) {
        super(timestamp);
        this.name = name;
        this.warning = warning;
    }

    public Boolean isWarning() {
        return warning;
    }

    public void setWarning(Boolean warning) {
        this.warning = warning;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
