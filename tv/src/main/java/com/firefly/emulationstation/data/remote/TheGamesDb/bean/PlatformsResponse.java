package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

public class PlatformsResponse extends Response {
    private PlatformsData data;

    public PlatformsData getData() {
        return data;
    }

    public void setData(PlatformsData data) {
        this.data = data;
    }
}
