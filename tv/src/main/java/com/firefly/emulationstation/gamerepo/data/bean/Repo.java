package com.firefly.emulationstation.gamerepo.data.bean;

import android.text.TextUtils;

import com.firefly.emulationstation.utils.I18nHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by rany on 18-4-24.
 */

public final class Repo {
    private String id;
    private Map<String, String> name;
    private Map<String, String> description;
    private String version;
    private String url;
    private List<Plat> platforms;
    private int compatible;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        String n = I18nHelper.getValueFromMap(name);

        if (n != null) {
            return n;
        }

        return id;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public String getDescription() {
        String desc = I18nHelper.getValueFromMap(description);

        if (desc != null) {
            return desc;
        }

        return null;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Plat> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<Plat> platforms) {
        this.platforms = platforms;
    }

    public int getCompatible() {
        return compatible;
    }

    public void setCompatible(int compatible) {
        this.compatible = compatible;
    }

    public List<String> getSupportPlatform() {
        List<String> plats = new ArrayList<>();

        if (platforms == null) {
            return plats;
        }

        for (Plat plat : platforms) {
            plats.add(plat.getPlatformId());
        }

        return plats;
    }

    public boolean isSupport(String platformId) {
        if (platformId == null || platformId.isEmpty() || platforms == null) {
            return false;
        }

        for (Plat plat : platforms) {
            if (platformId.equals(plat.getPlatformId())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check the repo is valid or invalid
     * @return true if the repo is valid or false
     */
    public boolean selfCheck() {
        if (TextUtils.isEmpty(id)) {
            return false;
        } else if (name == null || name.isEmpty()) {
            return false;
        } else if (TextUtils.isEmpty(version)) {
            return false;
        } else if (TextUtils.isEmpty(url)) {
            return false;
        } else if (platforms == null || platforms.isEmpty()) {
            return false;
        }

        return true;
    }

    public static Repo fromFile(File file) throws IOException {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.setLenient(true);
        return gson.fromJson(reader, Repo.class);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Repo)) {
            return false;
        } else if (!id.equals(((Repo) obj).getId())) {
            return false;
        }

        return true;
    }
}
