package org.micronurse.model;

public class FriendMoment {
    private Integer userId;
    private Long timestamp;
    private String textContent;

    public FriendMoment(int userId, long timestamp, String textContent){
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

    public Integer getUserId() {
        return userId;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
