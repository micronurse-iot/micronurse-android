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
    @Column(name = "UserId", notNull = true, index = true, unique = true)
    private int userId;

    @Column(name = "PhoneNumber", notNull = true, length = 20)
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

    public LoginUserRecord(int userId, String phoneNumber, String token, Bitmap portrait, Date lastLoginTime) {
        super();
        this.userId = userId;
        this.phoneNumber = phoneNumber;
        this.token = token;
        this.portrait = portrait;
        this.lastLoginTime = lastLoginTime;
    }

    public LoginUserRecord(int userId, String phoneNumber, String token, Bitmap portrait) {
        this(userId, phoneNumber, token, portrait, new Date());
    }

    public int getUserId() {
        return userId;
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
