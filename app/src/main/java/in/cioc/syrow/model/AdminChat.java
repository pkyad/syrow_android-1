package in.cioc.syrow.model;

import org.json.JSONObject;

public class AdminChat {
    public String pk, created, uid, attachment, message, attachmentType, user;
    public boolean sentByAgent;
    public JSONObject object;

    public AdminChat(JSONObject object) {
        this.object = object;
    }

    public String getPk() {
        return pk;
    }

    public void setPk(String pk) {
        this.pk = pk;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser() {
        return user;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public boolean isSentByAgent() {
        return sentByAgent;
    }

    public void setSentByAgent(boolean sentByAgent) {
        this.sentByAgent = sentByAgent;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }
}
