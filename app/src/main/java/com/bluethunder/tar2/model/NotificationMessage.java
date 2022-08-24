package com.bluethunder.tar2.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NotificationMessage {

    @SerializedName("data")
    @Expose
    private String data;
    @SerializedName("token")
    @Expose
    private List<String> token = null;

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

}