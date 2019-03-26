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

package com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean;

import android.support.annotation.NonNull;

import com.firefly.emulationstation.data.bean.Game;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/*
 * Game class represents video entity with name, description, image thumbs and video url.
 *
 */
@Root(name = "Game", strict = false)
public class TheGamesDbGame implements Serializable {
    @NonNull
    @Element(name = "GameTitle")
    private String name;
    @Element(name = "Overview", required = false)
    private String description;
    @Element(name = "Images", required = false)
    private TheGamesDbImages images;
    @Element(name = "Youtube", required = false)
    private String videoUrl;
    @Element(required = false)
    private String developer;
    @Element(required = false)
    private float rating;
    private String system;

    public TheGamesDbGame() {
        images = new TheGamesDbImages();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        if (images != null) {
            if (images.getFanarts() != null) {
                return TheGamesDbGames.baseImgUrl + images.getFanarts().get(0).getOriginal();
            } else if (images.getScreenshots() != null) {
                return TheGamesDbGames.baseImgUrl + images.getScreenshots().get(0).getOriginal();
            }
        }

        return null;
    }

    public String getCardImageUrl() {
        List<Boxart> boxartList = images.getBoxarts();
        if (images != null && boxartList != null) {
            for (Boxart boxart : boxartList) {
                if ("front".equals(boxart.getSide()))
                    return TheGamesDbGames.baseImgUrl + boxart.getThumb();
            }

            return TheGamesDbGames.baseImgUrl + images.getBoxarts().get(0).getThumb();
        }

        return null;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public TheGamesDbImages getImages() {
        return images;
    }

    public void setImages(TheGamesDbImages images) {
        this.images = images;
    }

    public Game toGame() {
        Game game = new Game();

        game.setName(name);
        game.setCardImageUrl(getCardImageUrl());
        game.setBackgroundImageUrl(getBackgroundImageUrl());
        if (game.getDescription("default") == null) {
            game.setDescription("default", getDescription());
        } else {
            game.setDescription("en", getDescription());
        }
        game.setRating(getRating());
        game.setScraped(true);

        return game;
    }
}
