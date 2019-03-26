package com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created by rany on 17-11-8.
 */

@Entity
@Root(name = "fanart")
public class Fanart implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @Element
    private String original;
    @Element
    private String thumb;

    private String game;

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
