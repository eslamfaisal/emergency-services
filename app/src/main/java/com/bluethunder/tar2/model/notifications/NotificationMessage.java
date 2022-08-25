package com.bluethunder.tar2.model.notifications;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

@Keep
public class NotificationMessage implements Serializable {

    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("topic")
    @Expose
    private String topic;

    @SerializedName("token")
    @Expose
    private List<String> token = null;

    public NotificationMessage() {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getToken() {
        return token;
    }

    public void setToken(List<String> token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "NotificationMessage{" +
                "data='" + data + '\'' +
                ", topic='" + topic + '\'' +
                ", token=" + token +
                '}';
    }
}