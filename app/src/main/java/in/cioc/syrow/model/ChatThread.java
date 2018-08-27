package in.cioc.syrow.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatThread {
    public String pk, created, uid, status, customerRating, customerFeedback, company, user, userDevice, userDeviceIp;
    public JSONObject object;

    public ChatThread(JSONObject object) throws JSONException {
        this.object = object;

        this.pk = object.getString("pk");
        this.created = object.getString("created");
        this.uid = object.getString("uid");
        this.status = object.getString("status");
        this.customerRating = object.getString("customerRating");
        this.customerFeedback = object.getString("customerFeedback");
        this.company = object.getString("company");
        this.user = object.getString("user");
        this.userDevice = object.getString("userDevice");
        this.userDeviceIp = object.getString("userDeviceIp");

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(String customerRating) {
        this.customerRating = customerRating;
    }

    public String getCustomerFeedback() {
        return customerFeedback;
    }

    public void setCustomerFeedback(String customerFeedback) {
        this.customerFeedback = customerFeedback;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserDevice() {
        return userDevice;
    }

    public void setUserDevice(String userDevice) {
        this.userDevice = userDevice;
    }

    public String getUserDeviceIp() {
        return userDeviceIp;
    }

    public void setUserDeviceIp(String userDeviceIp) {
        this.userDeviceIp = userDeviceIp;
    }

    public JSONObject getObject() {
        return object;
    }

    public void setObject(JSONObject object) {
        this.object = object;
    }
}
