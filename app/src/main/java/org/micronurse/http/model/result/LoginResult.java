package org.micronurse.http.model.result;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class LoginResult extends Result {
    public static final int LOGIN_USER_NOT_EXISTS = 200001;
    public static final int LOGIN_PASSWORD_INCORRECT = 200002;

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
