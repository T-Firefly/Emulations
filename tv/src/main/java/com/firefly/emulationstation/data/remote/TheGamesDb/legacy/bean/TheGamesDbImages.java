package com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rany on 17-11-9.
 */

@Root(name = "Images", strict = false)
public class TheGamesDbImages implements Serializable {
    @ElementList(inline = true, required = false)
    private List<Fanart> fanarts;
    @ElementList(inline = true, required = false)
    private List<Boxart> boxarts;
    @ElementList(inline = true, required = false)
    private List<Screenshot> screenshots;

    public TheGamesDbImages() {
    }

    public List<Fanart> getFanarts() {
        return fanarts;
    }

    public void setFanarts(List<Fanart> fanarts) {
        this.fanarts = fanarts;
    }

    public List<Boxart> getBoxarts() {
        return boxarts;
    }

    public void setBoxarts(List<Boxart> boxarts) {
        this.boxarts = boxarts;
    }

    public List<Screenshot> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<Screenshot> screenshots) {
        this.screenshots = screenshots;
    }
}
