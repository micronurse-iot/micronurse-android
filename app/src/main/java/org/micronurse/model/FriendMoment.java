package org.micronurse.model;

public class FriendMoment {
    private String userId;
    private long timestamp;
    private String textContent;

    public FriendMoment(String userId, long timestamp, String textContent){
        this.userId = userId;
        this.timestamp = timestamp;
        this.textContent = textContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
