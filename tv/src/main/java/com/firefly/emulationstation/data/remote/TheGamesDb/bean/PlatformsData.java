package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import java.io.Serializable;
import java.util.Map;

public class PlatformsData implements Serializable {
    private int count;
    private Map<Integer, Platform> platforms;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Map<Integer, Platform> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(Map<Integer, Platform> platforms) {
        this.platforms = platforms;
    }
}
