package com.bluethunder.tar2.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationRequestBody {

    @SerializedName("message")
    @Expose
    private NotificationMessage message;

    public NotificationMessage getMessage() {
        return message;
    }

    public void setMessage(NotificationMessage message) {
        this.message = message;
    }

}