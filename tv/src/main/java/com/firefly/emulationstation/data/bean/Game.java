/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.firefly.emulationstation.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.firefly.emulationstation.utils.I18nHelper;
import com.firefly.emulationstation.utils.Utils;

import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
 * Game class represents video entity with name, description, image thumbs and video url.
 *
 */
@Entity
@Root(name = "Game", strict = false)
public class Game implements Serializable {
    public static final int STATUS_NEW_GAME = 0;
    public static final int STATUS_RECOMMENDED = 1;
    public static final int STATUS_NORMAL = 2;
    public static final int STATUS_NOT_INSTALL = 3;

    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String name;
    private Map<String, String> displayNames;
    private Map<String, String> description;
    @NonNull
    private String path;
    private String backgroundImageUrl;
    private String cardImageUrl;
    private String icon;
    private String videoUrl;
    private String developer;
    private float rating;
    /**
     * This field use for store the platformId
     */
    private String platformId;
    /**
     * Generate from GameSystem
     * The ${@link #path} is always not equals, if this field is not equals.
     * If the ${@link #path} is not equals this field may be equals when the games
     * in the same rom path.
     */
    private String romPathId;
    private String ext;
    private boolean isScraped;
    /**
     * This value will load from GameSystemRef
     */
    private boolean isStar;
    private int status;

    /**
     * for which download from repo
     */
    private String repository;
    private int downloadId;
    private String url;
    private String version;
    private List<Dependency> dependencies;

    @Ignore
    private DownloadInfo downloadInfo;
    @Ignore
    private int progress = -1;
    @Ignore
    private boolean isSupport;

    public Game() {
    }

    /**
     * Merge new info to database Game
     * @param game which Game instance contain new info
     */
    public void merge(Game game) {
        backgroundImageUrl = game.getBackgroundImageUrl();
        cardImageUrl = game.getCardImageUrl();
        description = game.getDescription();
        rating = game.getRating();
        isScraped = game.isScraped();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public void setDescription(Map<String, String> description) {
        this.description = description;
    }

    public void setDescription(@NonNull String code, @NonNull String name) {
        if (description == null) {
            description = new HashMap<>();
        }
        description.put(code, name);
    }

    public String getDescription(String code) {
        if (description == null) {
            return null;
        }

        return description.get(code);
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getBackgroundImageUrl() {
        return backgroundImageUrl;
    }

    public void setBackgroundImageUrl(String bgImageUrl) {
        this.backgroundImageUrl = bgImageUrl;
    }

    public String getCardImageUrl() {
        return cardImageUrl;
    }

    public void setCardImageUrl(String cardImageUrl) {
        this.cardImageUrl = cardImageUrl;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getRomPathId() {
        return romPathId;
    }

    public void setRomPathId(String romPathId) {
        this.romPathId = romPathId;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public URI getBackgroundImageURI() {
        try {
            Log.d("BACK GAME: ", backgroundImageUrl);
            return new URI(getBackgroundImageUrl());
        } catch (URISyntaxException e) {
            Log.d("URI exception: ", backgroundImageUrl);
            return null;
        }
    }

    public URI getCardImageURI() {
        try {
            return new URI(getCardImageUrl());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    /*
     * Don't modify this function, the Room will get value from this method.
     */
    public Map<String, String> getDisplayNames() {
        return displayNames;
    }

    public void setDisplayNames(Map<String, String> displayNames) {
        this.displayNames = displayNames;
    }

    public String getDisplayName() {
        String displayName = I18nHelper.getValueFromMap(displayNames);

        if (TextUtils.isEmpty(displayName)) {
            return name;
        }

        return displayName;
    }

    public String getDisplayName(String code) {
        if (displayNames == null) {
            return null;
        }

        return displayNames.get(code);
    }

    public void setDisplayName(@NonNull String code, @NonNull String name) {
        if (displayNames == null) {
            displayNames = new HashMap<>();
        }
        displayNames.put(code, name);
    }

    public boolean isScraped() {
        return isScraped;
    }

    public void setScraped(boolean scraped) {
        isScraped = scraped;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (repository == null ? 0 : repository.hashCode());
        hash = 31 * hash + (TextUtils.isEmpty(path) ? 0 : path.hashCode());
        hash = 31 * hash + name.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Game)) {
            return false;
        }

        Game game = (Game) obj;
        // The path is not equals can not consider the game is not equals.
        // In case of the downloading repo game, the path is not the final path,
        // when download completed it may refine the path.
        if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(game.getPath())
                && path.equals(game.getPath())) {
            return true;
        } else {
            // romPathId identity the games on the same path, because the game download from
            // repo is always in the same path.
            // The repository, romPathId and name is not empty and equals the game download from repo
            // is the same game.
            // The path is not always the same
            return (repository != null && repository.equals(game.getRepository()))
                    && name.equals(game.getName()) && romPathId.equals(game.getRomPathId());
        }
    }

    public int getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(int downloadId) {
        this.downloadId = downloadId;
    }

    public DownloadInfo getDownloadInfo() {
        if (downloadInfo != null) {
            downloadInfo.setRef(this);
        }
        return downloadInfo;
    }

    public void setDownloadInfo(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    public String getUrl() {
        return url;
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

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
}
