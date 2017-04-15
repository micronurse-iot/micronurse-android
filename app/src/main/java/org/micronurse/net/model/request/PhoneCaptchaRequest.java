package org.micronurse.net.model.request;

/**
 * Created by shengyun-zhou on 6/10/16.
 */
public class PhoneCaptchaRequest {
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    private String phoneNumber;

    public PhoneCaptchaRequest(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
