package org.micronurse.database.model;

import android.graphics.Bitmap;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;

/**
 * Created by zhou-shengyun on 7/30/16.
 */

@Table(name = "LoginUserRecord")
public class LoginUserRecord extends Model {
    @Column(name = "PhoneNumber", notNull = true, length = 20, index = true, unique = true)
    private String phoneNumber;

    @Column(name = "Token", length = 40)
    private String token;

    @Column(name = "Portrait", notNull = true)
    private Bitmap portrait;

    @Column(name = "LastLoginTime", notNull = true)
    private Date lastLoginTime;

    public LoginUserRecord(){
        super();
    }

    public LoginUserRecord(String phoneNumber, String token, Bitmap portrait, Date lastLoginTime) {
        super();
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.portrait = portrait;
        this.lastLoginTime = lastLoginTime;
    }

    public LoginUserRecord(String phoneNumber, String token, Bitmap portrait) {
        this(phoneNumber, token, portrait, new Date());
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Bitmap getPortrait() {
        return portrait;
    }

    public void setPortrait(Bitmap portrait) {
        this.portrait = portrait;
    }

    public Date getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Date lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
