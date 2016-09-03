package org.micronurse.model;

/**
 * Created by zhou-shengyun on 9/3/16.
 */
public class GPS extends Sensor {
    private Double longitude;
    private Double latitude;

    public GPS(long timestamp, Double longitude, Double latitude) {
        super(timestamp);
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
