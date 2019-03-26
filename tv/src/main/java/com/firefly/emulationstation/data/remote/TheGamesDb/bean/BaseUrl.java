package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class BaseUrl implements Serializable {
    private String original;
    private String small;
    private String thumb;
    @SerializedName("cropped_center_thumb")
    private String croppedCenterThumb;
    private String medium;
    private String large;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getSmall() {
        return small;
    }

    public void setSmall(String small) {
        this.small = small;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getCroppedCenterThumb() {
        return croppedCenterThumb;
    }

    public void setCroppedCenterThumb(String croppedCenterThumb) {
        this.croppedCenterThumb = croppedCenterThumb;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }
}
