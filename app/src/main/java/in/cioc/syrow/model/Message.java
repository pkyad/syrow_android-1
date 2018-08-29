package in.cioc.syrow.model;

import java.io.Serializable;

/**
 * Created by Lincoln on 07/01/16.
 */
public class Message implements Serializable {
    String id, message, messageImg, createdAt;
    boolean sentByAgent;
    User user;

    public Message() {
    }

    public Message(String id, String message, String messageImage, String createdAt, User user) {
        this.id = id;
        this.message = message;
        this.messageImg = messageImage;
        this.createdAt = createdAt;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageImg() {
        return messageImg;
    }

    public void setMessageImg(String messageImg) {
        this.messageImg = messageImg;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isSentByAgent() {
        return sentByAgent;
    }

    public void setSentByAgent(boolean sentByAgent) {
        this.sentByAgent = sentByAgent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
