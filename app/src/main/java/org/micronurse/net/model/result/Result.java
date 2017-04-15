package org.micronurse.net.model.result;

/**
 * Created by shengyun-zhou on 5/23/16.
 */
public class Result {
    private int resultCode;
    private String message;

    public Result(int resultCode, String message) {
        this.resultCode = resultCode;
        this.message = message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
