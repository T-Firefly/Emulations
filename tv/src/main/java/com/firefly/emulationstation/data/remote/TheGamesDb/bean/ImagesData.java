package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class ImagesData implements Serializable {
    @SerializedName("base_url")
    private BaseUrl baseUrl;
    private Map<Long, Image[]> images;

    public BaseUrl getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(BaseUrl baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Map<Long, Image[]> getImages() {
        return images;
    }

    public void setImages(Map<Long, Image[]> images) {
        this.images = images;
    }
}
