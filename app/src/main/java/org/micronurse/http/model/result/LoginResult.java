package org.micronurse.http.model.result;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class LoginResult extends Result {
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
