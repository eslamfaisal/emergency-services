package com.bluethunder.tar2.ui.auth.model;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public final class UserModel implements Serializable {
    private String id;

    private String pushToken;

    private String name;

    private String password;

    private String phone;

    private String imageUrl;

    private String countryCode;

    private int unReadChatCount = 0;

    public UserModel() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getUnReadChatCount() {
        return unReadChatCount;
    }

    public void setUnReadChatCount(int unReadChatCount) {
        this.unReadChatCount = unReadChatCount;
    }
}
