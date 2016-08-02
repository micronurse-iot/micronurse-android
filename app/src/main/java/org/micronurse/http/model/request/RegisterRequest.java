package org.micronurse.http.model.request;

/**
 * @author buptsse-zero <GGGZ-1101-28@Live.cn>
 */
public class RegisterRequest {
    private String phoneNumber;
    private String password;
    private String nickname;
    private char gender;
    private char accountType;
    private String captcha;

    public RegisterRequest(){}

    public RegisterRequest(String phoneNumber, String password, String nickname, char gender, char accountType, String captcha) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.nickname = nickname;
        this.gender = gender;
        this.accountType = accountType;
        this.captcha = captcha;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public char getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public char getAccountType() {
        return accountType;
    }

    public void setAccountType(char accountType) {
        this.accountType = accountType;
    }
}
