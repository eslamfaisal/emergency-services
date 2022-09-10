package com.bluethunder.tar2.ui.onboarding.model;

import androidx.annotation.DrawableRes;

public class BoardingModel {

    private String title;
    private String description;
    private @DrawableRes
    int image;

    public BoardingModel() {
    }

    public BoardingModel(String title, String description, @DrawableRes int image) {
        this.title = title;
        this.description = description;
        this.image = image;
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

    public int getImage() {
        return image;
    }

    public void setImage(@DrawableRes int image) {
        this.image = image;
    }
}
