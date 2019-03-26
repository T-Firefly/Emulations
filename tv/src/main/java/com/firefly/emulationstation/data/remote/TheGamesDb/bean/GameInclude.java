package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import java.io.Serializable;

public class GameInclude implements Serializable {
    private Boxart boxart;
//    private Platforms platform;


    public Boxart getBoxart() {
        return boxart;
    }

    public void setBoxart(Boxart boxart) {
        this.boxart = boxart;
    }
}
