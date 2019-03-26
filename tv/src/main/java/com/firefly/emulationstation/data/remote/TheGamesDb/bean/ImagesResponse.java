package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

public class ImagesResponse extends Response {
    private ImagesData data;

    public ImagesData getData() {
        return data;
    }

    public void setData(ImagesData data) {
        this.data = data;
    }
}
