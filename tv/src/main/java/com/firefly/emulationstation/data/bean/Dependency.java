package com.firefly.emulationstation.data.bean;

import java.io.Serializable;

/**
 * Created by rany on 18-4-24.
 */

public final class Dependency implements Serializable {
    private String name;
    private String url;
    private String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url == null ? null : url.trim();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
