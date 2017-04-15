package org.micronurse.net.model.request;

public class SaveHomeLocationRequest {
    private Double homeLongitude;
    private Double homeLatitude;
    private String address;

    public SaveHomeLocationRequest(Double homeLongitude, Double homeLatitude, String address){
        this.homeLongitude = homeLongitude;
        this.homeLatitude = homeLatitude;
        this.address = address;
    }

    public Double getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomeLongitude(Double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    public Double getHomeLatitude() {
        return homeLatitude;
    }

    public void setHomeLatitude(Double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
