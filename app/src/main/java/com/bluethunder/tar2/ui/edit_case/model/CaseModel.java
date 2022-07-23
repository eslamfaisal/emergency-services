
package com.bluethunder.tar2.ui.edit_case.model;

import java.io.Serializable;

public final class CaseModel implements Serializable {
    private String id;

    private String userId;

    private String categoryId;

    private String title;

    private String description;

    private String images;

    private String mainImage;

    private String latitude;

    private String longitude;

    private Boolean showUserData;

    private Boolean hasPhoneCall;

    private String hasOnlineCall;

    private String hasVideoCall;

    private String locationName;

    private String address;


    public CaseModel() {
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

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public String getMainImage() {
        return mainImage;
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

    public String getAddress() {
        return address;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public String toString() {
        return "CaseModel{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", images='" + images + '\'' +
                ", mainImage='" + mainImage + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", showUserData=" + showUserData +
                ", hasPhoneCall=" + hasPhoneCall +
                ", hasOnlineCall='" + hasOnlineCall + '\'' +
                ", hasVideoCall='" + hasVideoCall + '\'' +
                ", locationName='" + locationName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
