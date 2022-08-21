package com.bluethunder.tar2.ui.chat.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class ChatHead implements Serializable {

    private String id;
    private String caseTitle;
    private String caseDescription;
    private String[] users;
    @ServerTimestamp
    private Date lastMessageAt;
    private String caseUserId;
    private String chatSenderId;
    private String caseImage;
    private String lastMessage;

    public ChatHead() {
    }

    public ChatHead(String id, String caseTitle, String caseDescription, String caseImage,
                    String[] users, Date lastMessageAt, String caseUserId,
                    String chatSenderId, String lastMessage) {
        this.id = id;
        this.caseTitle = caseTitle;
        this.caseDescription = caseDescription;
        this.caseImage = caseImage;
        this.users = users;
        this.lastMessageAt = lastMessageAt;
        this.caseUserId = caseUserId;
        this.chatSenderId = chatSenderId;
        this.lastMessage = lastMessage;
    }

    public String getCaseImage() {
        return caseImage;
    }

    public void setCaseImage(String caseImage) {
        this.caseImage = caseImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaseTitle() {
        return caseTitle;
    }

    public void setCaseTitle(String caseTitle) {
        this.caseTitle = caseTitle;
    }

    public String getCaseDescription() {
        return caseDescription;
    }

    public void setCaseDescription(String caseDescription) {
        this.caseDescription = caseDescription;
    }

    public String[] getUsers() {
        return users;
    }

    public void setUsers(String[] users) {
        this.users = users;
    }

    public Date getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Date lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getCaseUserId() {
        return caseUserId;
    }

    public void setCaseUserId(String caseUserId) {
        this.caseUserId = caseUserId;
    }

    public String getChatSenderId() {
        return chatSenderId;
    }

    public void setChatSenderId(String chatSenderId) {
        this.chatSenderId = chatSenderId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
