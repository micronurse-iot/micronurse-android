package org.micronurse.http.model;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class LoginResult extends Result {
    public static final int LOGIN_USER_NOT_EXISTS = 101;
    public static final int LOGIN_PASSWORD_INCORRECT = 102;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
