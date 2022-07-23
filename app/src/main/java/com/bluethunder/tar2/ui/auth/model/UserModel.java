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

    public UserModel() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

}
