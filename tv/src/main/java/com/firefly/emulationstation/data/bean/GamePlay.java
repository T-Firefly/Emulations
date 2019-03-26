package com.firefly.emulationstation.data.bean;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.io.Serializable;

@Root
public class GamePlay implements Serializable {
    @Attribute
    private String name;
    @Attribute(required = false)
    private int versionCode;
    @Attribute(required = false)
    private String version;
    @Text
    private String url;
    @Attribute(required = false)
    private boolean isCleanInstall = false;
    @Attribute(required = false)
    private boolean isCompress = true;

    public String getName() {
        return name.replace(".zip", "");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return name;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url == null ? null : url.trim();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCleanInstall() {
        return isCleanInstall;
    }

    public void setCleanInstall(boolean cleanInstall) {
        isCleanInstall = cleanInstall;
    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setCompress(boolean compress) {
        isCompress = compress;
    }
}
