package org.micronurse.http.model.result;


import org.micronurse.http.model.request.SaveHomeLocationRequest;

public class HomeLocationResult extends Result{
    private Double latitude;
    private Double longitude;
    public HomeLocationResult(int resultCode, String message, Double latitude, Double longitude){
        super(resultCode, message);
        this.latitude = latitude;
        this.longitude =longitude;
    };
    public Double getLatitude(){
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
