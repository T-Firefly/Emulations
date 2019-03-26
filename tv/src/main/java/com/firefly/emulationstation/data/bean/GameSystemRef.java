package com.firefly.emulationstation.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by rany on 18-4-28.
 */

@Entity(primaryKeys = { "gameId", "system" },
        foreignKeys = {
                @ForeignKey(entity = Game.class,
                        parentColumns = "id",
                        childColumns = "gameId",
                        onDelete = CASCADE
                )
        })
public class GameSystemRef {
    private long gameId;
    @NonNull
    private String system;
    private boolean isStar;

    public long getGameId() {
        return gameId;
    }

    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }
}
