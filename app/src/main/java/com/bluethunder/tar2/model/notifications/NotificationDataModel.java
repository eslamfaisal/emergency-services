package com.bluethunder.tar2.model.notifications;

import androidx.annotation.Keep;

import com.huawei.wisesecurity.kfs.validation.constrains.KfsIntegerRange;

import java.io.Serializable;

@Keep
public class NotificationDataModel implements Serializable {

    private String userId;
    private String caseId;
    private String title;
    private String description;
    private String type;

    public NotificationDataModel() {
    }

    public NotificationDataModel(String userId, String caseId, String title, String description, String type) {
        this.userId = userId;
        this.caseId = caseId;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
