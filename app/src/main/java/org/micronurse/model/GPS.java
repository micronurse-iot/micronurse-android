package org.micronurse.model;

import java.util.Date;

/**
 * Created by zhou-shengyun on 9/3/16.
 */
public class GPS extends Sensor {
    private Double longitude;
    private Double latitude;
    private String address;

    public GPS(Date timestamp, Double longitude, Double latitude, String address) {
        super(timestamp);
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }
}
