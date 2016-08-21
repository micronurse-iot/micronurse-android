package org.micronurse.http.model.result;

import org.micronurse.model.User;

/**
 * Created by zhou-shengyun on 7/1/16.
 */
public class UserResult extends Result {
    private User user;

    public UserResult(int resultCode, String message, User user) {
        super(resultCode, message);
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
