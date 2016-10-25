package org.micronurse.http.model.request;

public class SaveHomeLocationRequest {
    private Double homeLongitude;
    private Double homeLatitude;

    public SaveHomeLocationRequest(){
    }

    public SaveHomeLocationRequest(Double homeLongitude, Double homeLatitude){
        this.homeLongitude = homeLongitude;
        this.homeLatitude = homeLatitude;
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
}
