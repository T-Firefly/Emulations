package com.firefly.emulationstation.gamerepo.data.bean;

import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.Dependency;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.utils.I18nHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by rany on 18-4-24.
 */

public final class Rom {
    private String name;
    private Map<String, String> displayName;
    private Map<String, String> description;
    private String boxart;
    private String fanart;
    private String developer;
    private String system;
    private String url;
    private String version;
    private List<Dependency> dependencies;

    private String repo;

    /**
     * Hold the downloaded Game in database.
     */
    private Game game;

    /**
     * Only for official repo
     */
    private boolean isRecommended;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBasename() {
        int index = name.lastIndexOf('.');

        if (index == -1) {
            return name;
        }

        return name.substring(0, index);
    }

    public Map<String, String> getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Map<String, String> displayName) {
        this.displayName = displayName;
    }

    public Map<String, String> getDescriptions() {
        return description;
    }

    public String getDescription() {
        return I18nHelper.getValueFromMap(description);
    }

    public void setDescriptions(Map<String, String> description) {
        this.description = description;
    }

    public String getBoxart() {
        return boxart;
    }

    public void setBoxart(String boxart) {
        this.boxart = boxart;
    }

    public String getFanart() {
        return fanart;
    }

    public void setFanart(String fanart) {
        this.fanart = fanart;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getUrl() {
        return url == null ? null : url.trim();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExt() {
        int index = name.lastIndexOf('.');
        String ext = null;

        if (index != -1) {
            ext = name.substring(index);
        }

        if (TextUtils.isEmpty(ext)) {
            String filename = URLUtil.guessFileName(url, null, null);
            index = filename.lastIndexOf('.');
            if (index != -1) {
                ext = filename.substring(index);
            }
        }

        if (ext != null && !ext.startsWith(".")) {
            ext = '.' + ext;
        }

        return ext;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public static List<Rom> fromFile(File file) throws IOException {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.setLenient(true);
        return gson.fromJson(reader, new TypeToken<List<Rom>>(){}.getType());
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public Game toGame(GameSystem gameSystem) {
        Game game = null;
        if (getGame() == null) {
            game = new Game();
            game.setName(getBasename());
            game.setExt(getExt());
            game.setPlatformId(getSystem());
            game.setDescription(getDescriptions());
            game.setCardImageUrl(getBoxart());
            game.setBackgroundImageUrl(getFanart());
            game.setDeveloper(getDeveloper());
            game.setDisplayNames(getDisplayName());
            game.setRepository(getRepo());
            game.setScraped(true);
            game.setPath(savePath(gameSystem));
            game.setRomPathId(gameSystem.getRomPathID());
            game.setUrl(getUrl());
            game.setVersion(getVersion());
        } else {
            game = this.game;
        }

        game.setStatus(Game.STATUS_NOT_INSTALL);
        game.setDependencies(getDependencies());

        return game;
    }

    private String savePath(GameSystem gameSystem) {
        String romPath = gameSystem.getRomPath();
        String name = getName();
        StringBuilder builder = new StringBuilder(romPath);

        if (!romPath.endsWith("/")) {
            builder.append('/');
        }

        builder.append(getRepo());
        builder.append('/');

        builder.append(name);

        String path = builder.toString();
        if (path.startsWith(Constants.ROM_PATH_DEVICE_PREFIX)) {
            path = path.replace(Constants.ROM_PATH_DEVICE_PREFIX,
                    Environment.getExternalStorageDirectory().getPath());
        }

        return path;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isRecommended() {
        return isRecommended;
    }

    public void setRecommended(boolean recommended) {
        isRecommended = recommended;
    }
}
