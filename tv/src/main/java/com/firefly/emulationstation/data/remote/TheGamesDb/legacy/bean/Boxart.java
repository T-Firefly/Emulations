package com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean;

import android.arch.persistence.room.Entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.io.Serializable;

/**
 * Created by rany on 17-11-9.
 */

@Entity
@Root(name = "boxart", strict = false)
public class Boxart implements Serializable {
    @Attribute(required = false)
    private String side;
    @Attribute
    private int width;
    @Attribute
    private int height;
    @Attribute
    private String thumb;
    @Text
    private String url;

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
