package com.firefly.emulationstation.data.bean;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rany on 17-10-31.
 */

@Root(name = "systems")
public class GameSystems {
    @ElementList(name = "system", inline = true)
    private List<GameSystem> systems;

    public List<GameSystem> getSystems() {
        return systems;
    }

    public void setSystems(List<GameSystem> systems) {
        this.systems = systems;
    }
}
