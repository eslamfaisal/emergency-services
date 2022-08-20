package com.bluethunder.tar2.ui.chat.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class ChatHead implements Serializable {

    private String id;
    private String userID;
    private Message message;
    private String userName;
    @ServerTimestamp
    private Date date;
    private String token;
    private boolean read;

    public ChatHead() {
    }

    public ChatHead(String id, String userID, Message message, String userName, Date date, String token, boolean read) {
        this.id = id;
        this.userID = userID;
        this.message = message;
        this.userName = userName;

        this.date = date;
        this.token = token;

        this.read = read;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
