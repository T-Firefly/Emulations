package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class Boxart implements Serializable {
    @SerializedName("base_url")
    private BaseUrl baseUrl;
    private Map<Long, Image[]> data;

    public BaseUrl getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(BaseUrl baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Map<Long, Image[]> getData() {
        return data;
    }

    public void setData(Map<Long, Image[]> data) {
        this.data = data;
    }
}
