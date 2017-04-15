package org.micronurse.net.model.request;

/**
 * Created by shengyun-zhou on 6/12/16.
 */
public class ModifyPasswordRequest {
    private String oldPassword;
    private String newPassword;

    public ModifyPasswordRequest(){}

    public ModifyPasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }


    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
