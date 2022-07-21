/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2020. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */
package com.bluethunder.tar2.ui.edit_case.model;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.Indexes;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType CaseModel.
 *
 * @since 2022-07-22
 */
@PrimaryKeys({"id"})
@Indexes({"id:id", "userId:userId", "categoryId:categoryId"})
public final class CaseModel extends CloudDBZoneObject {
    private String id;

    private String userId;

    private String categoryId;

    private String title;

    private String description;

    private String images;

    private String latitude;

    private String longitude;

    private Boolean showUserData;

    private Boolean hasPhoneCall;

    private String hasOnlineCall;

    private String hasVideoCall;

    public CaseModel() {
        super(CaseModel.class);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getImages() {
        return images;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setShowUserData(Boolean showUserData) {
        this.showUserData = showUserData;
    }

    public Boolean getShowUserData() {
        return showUserData;
    }

    public void setHasPhoneCall(Boolean hasPhoneCall) {
        this.hasPhoneCall = hasPhoneCall;
    }

    public Boolean getHasPhoneCall() {
        return hasPhoneCall;
    }

    public void setHasOnlineCall(String hasOnlineCall) {
        this.hasOnlineCall = hasOnlineCall;
    }

    public String getHasOnlineCall() {
        return hasOnlineCall;
    }

    public void setHasVideoCall(String hasVideoCall) {
        this.hasVideoCall = hasVideoCall;
    }

    public String getHasVideoCall() {
        return hasVideoCall;
    }

}
