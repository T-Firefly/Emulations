package com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rany on 17-10-27.
 */

@Root(name = "Data")
public class TheGamesDbGames {
    @Element
    public static String baseImgUrl = "http://thegamesdb.net/banners/";
    @ElementList(inline = true, required = false)
    private List<TheGamesDbGame> games;

    public List<TheGamesDbGame> getGames() {
        return games;
    }

    public void setGames(List<TheGamesDbGame> games) {
        this.games = games;
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public void setBaseImgUrl(String baseImgUrl) {
        TheGamesDbGames.baseImgUrl = baseImgUrl;
    }
}
