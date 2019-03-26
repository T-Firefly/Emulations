package com.firefly.emulationstation.data.local;

import android.content.Context;
import android.support.annotation.NonNull;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.utils.Sha1Helper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rany on 17-10-26.
 */

public class GameNameFromMapFile {
    private Context mContext;
    private Map<String, String> mGames = new HashMap<>();
    private Map<String, String> mCustomGames = new HashMap<>();
    private Map<String, String> mHashGames = new HashMap<>();

    @Inject
    public GameNameFromMapFile(Context context) {
        mContext = context;
    }

    public GameNameFromMapFile setMapFileName(String name) {
        if (mCustomGames.size() <= 0) {
            initCustomMap(name);
        }

        if (mGames.size() <= 0) {
            initInternalMap(name);
        }

        if (mHashGames.size() <= 0) {
            initHashMap();
        }

        return this;
    }

    private void initInternalMap(String name) {
        try {
            InputStream is = mContext.getAssets().open(name +".txt");
            initMap(is, mGames);
        } catch (IOException ignored) {
        }

    }

    private void initCustomMap(String name) {
        try {
            InputStream is = new FileInputStream(Constants.ES_MAP_DIR + "/custom/" + name + ".txt");
            initMap(is, mCustomGames);
        } catch (IOException ignored) {
        }
    }

    private void initMap(InputStream is, Map<String, String> games) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#"))
                continue;

            String names[] = line.split(":::");
            if (names.length < 2) {
                continue;
            }

            games.put(names[0].replace("\"", ""),
                    names[1].replace("\"", ""));
        }
    }

    private void initHashMap() {
        InputStream is = null;
        try {
            is = new FileInputStream(Constants.ES_MAP_DIR + "/hash.csv");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                String names[] = line.split(",", 4);
                if (names.length <= 0)
                    continue;

                mHashGames.put(names[0], names[names.length-1].replace("\"", ""));
            }
        } catch (IOException ignored) {
        }
    }

    public Map<String, String> getGames() {
        return mGames;
    }

    public String getDisplayName(String key, String path) {
        String name = mCustomGames.get(key);
        if (name != null) {
            return name;
        }

        name = mGames.get(key);
        if (name != null) {
            return name;
        }

        name = getHashMapName(key, path);

        return name;
    }

    private String getHashMapName(String key, String path) {
        String name = null;

        try {
            String sha1sum = Sha1Helper.sha1(path);
            name = mHashGames.get(sha1sum);
        } catch (Exception e) {
            name = mHashGames.get(key);
        }

        if (name != null) {
            if (!name.startsWith(":", 2)) {
                name = "en:" + name;
            }
            return name;
        }

        return null;
    }

    public boolean isInit() {
        return !mCustomGames.isEmpty() || !mGames.isEmpty() || !mHashGames.isEmpty();
    }

    public void clear() {
        mCustomGames.clear();
        mGames.clear();
        mHashGames.clear();
    }
}
