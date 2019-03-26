package com.firefly.emulationstation.data.bean;

import com.firefly.emulationstation.commom.Constants;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;

/**
 * Created by rany on 17-10-31.
 */

@Root
public class Emulator implements Serializable{
    @Element
    private String name;
    @Element
    private String core;
    @Element(required = false)
    private String config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCore() {
        return core;
    }

    public void setCore(String core) {
        this.core = core;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getCorePath() {
        if (core.startsWith("${ES_DIR}")) {
            return core.replace("${ES_DIR}", Constants.ES_DIR);
        }

        return core;
    }
}
