package org.micronurse.net.model.result;

import org.micronurse.model.User;
import java.util.List;

/**
 * Created by zhou-shengyun on 8/28/16.
 */
public class UserListResult extends Result {
    private List<User> userList;

    public UserListResult(int resultCode, String message, List<User> userList) {
        super(resultCode, message);
        this.userList = userList;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
