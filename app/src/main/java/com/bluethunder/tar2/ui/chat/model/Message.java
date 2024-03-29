package com.bluethunder.tar2.ui.chat.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {

    private String id;

    private String type;

    @ServerTimestamp
    private Date date;

    private String content;

    private String messageFrom;

    private boolean showTime = true;

    public Message() {
    }


    public Message(String id, String type, Date date,
                   String content, String messageFrom,
                   boolean showTime) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.content = content;
        this.messageFrom = messageFrom;
        this.showTime = showTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    public String getMessageFrom() {
        return messageFrom;
    }

    public void setMessageFrom(String messageFrom) {
        this.messageFrom = messageFrom;
    }
}