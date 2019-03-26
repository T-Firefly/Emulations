package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import java.io.Serializable;
import java.util.List;

public class GameData implements Serializable {
    private long count;
    private List<RemoteGame> games;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<RemoteGame> getGames() {
        return games;
    }

    public void setGames(List<RemoteGame> games) {
        this.games = games;
    }
}
