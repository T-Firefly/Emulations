package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

public class GamesResponse extends Response {
    private GameData data;
    private GameInclude include;

    public GameData getData() {
        return data;
    }

    public void setData(GameData data) {
        this.data = data;
    }

    public GameInclude getInclude() {
        return include;
    }

    public void setInclude(GameInclude include) {
        this.include = include;
    }
}
