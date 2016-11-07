package org.micronurse.http.model.request;

public class PostFriendMomentRequest {
    private String textContent;

    public PostFriendMomentRequest(String textContent){
        this.textContent = textContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }
}
