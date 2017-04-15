package org.micronurse.model;

import java.util.Date;

public class FriendMoment {
    private Integer userId;
    private Date timestamp;
    private String textContent;

    public FriendMoment(int userId, Date timestamp, String textContent){
        this.userId = userId;
        this.timestamp = timestamp;
        this.textContent = textContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
