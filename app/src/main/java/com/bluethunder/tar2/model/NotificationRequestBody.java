package com.bluethunder.tar2.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Keep
public class NotificationRequestBody implements Serializable {

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