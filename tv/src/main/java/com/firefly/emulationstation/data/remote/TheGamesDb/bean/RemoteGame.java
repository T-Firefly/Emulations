package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import com.firefly.emulationstation.data.bean.Game;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RemoteGame implements Serializable {
    private long id;
    @SerializedName("game_title")
    private String gameTitle;
    @SerializedName("release_date")
    private String releaseDate;
    private int platform;
    private String overview;
    private int[] developers;

    private int players;
    private String[] publishers;
    private String[] genres;
    @SerializedName("last_updated")
    private String lastUpdated;
    private float rating;
    private String coop;
    private String youtube;
    private String os;
    private String processor;
    private String ram;
    private String hdd;
    private String[] video;
    private String sound;
    private String[] alternates;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGameTitle() {
        return gameTitle;
    }

    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public int[] getDevelopers() {
        return developers;
    }

    public void setDevelopers(int[] developers) {
        this.developers = developers;
    }

    public int getPlayers() {
        return players;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public String[] getPublishers() {
        return publishers;
    }

    public void setPublishers(String[] publishers) {
        this.publishers = publishers;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getCoop() {
        return coop;
    }

    public void setCoop(String coop) {
        this.coop = coop;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getHdd() {
        return hdd;
    }

    public void setHdd(String hdd) {
        this.hdd = hdd;
    }

    public String[] getVideo() {
        return video;
    }

    public void setVideo(String[] video) {
        this.video = video;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String[] getAlternates() {
        return alternates;
    }

    public void setAlternates(String[] alternates) {
        this.alternates = alternates;
    }

    public Game toGame() {
        Game game = new Game();
        game.setName(getGameTitle());
        if (game.getDescription("default") == null) {
            game.setDescription("default", getOverview());
        }
        game.setDescription("en", getOverview());
        game.setRating(getRating());
        game.setScraped(true);

        return game;
    }
}
