package org.micronurse.net.model.result;


public class HomeLocationResult extends Result{
    private Double latitude;
    private Double longitude;
    private String address;

    public HomeLocationResult(int resultCode, String message, Double latitude, Double longitude, String address){
        super(resultCode, message);
        this.latitude = latitude;
        this.longitude =longitude;
        this.address = address;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
