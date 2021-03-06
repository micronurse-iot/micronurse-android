package org.micronurse.model;

import android.graphics.Bitmap;

/**
 * Created by zhou-shengyun on 7/1/16.
 */
public class User{
    public static final char ACCOUNT_TYPE_OLDER = 'O';
    public static final char ACCOUNT_TYPE_GUARDIAN = 'G';
    public static final char GENDER_MALE = 'M';
    public static final char GENDER_FEMALE = 'F';

    private Long userId;
    private String phoneNumber;
    private String nickname;
    private Character gender;
    private Character accountType;
    private Bitmap portrait;

    public User(String phoneNumber, String nickname, char gender, char accountType) {
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.gender = gender;
        this.accountType = accountType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Character getAccountType() {
        return accountType;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Character getGender() {
        return gender;
    }

    public void setGender(char gender) {
        this.gender = gender;
    }

    public Bitmap getPortrait() {
        return portrait;
    }

    public void setPortrait(Bitmap portrait) {
        this.portrait = portrait;
    }
}
