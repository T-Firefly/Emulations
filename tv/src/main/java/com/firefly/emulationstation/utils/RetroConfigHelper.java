package com.firefly.emulationstation.utils;

import com.firefly.emulationstation.commom.Constants;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;

/**
 * Created by rany on 17-11-16.
 */

public class RetroConfigHelper {
    private static Map<String, String> CONFIGS = new HashMap<>();
    private static boolean INIT_CONFIGS = false;

    /*
     * For retroarch.cfg only
     *
     */
    public static String getConfigValue(String key) throws IOException {
        if (CONFIGS.get(key) != null) {
            return CONFIGS.get(key);
        }

        String result = getConfigValueFromFile(Constants.RETRO_CONFIG_FILE, key);

        if (result != null) {
            CONFIGS.put(key, result);
        }

        return result;
    }

    public static String getConfigValueFromFile(String filePath, String key) throws IOException {
        FileReader fileReader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;
        String result = null;

        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith(key)) {
                String[] map = line.split("=", 2);
                if (map.length == 2) {
                    result = map[1].trim().replace("\"", "");
                    break;
                }
            }
        }

        return result;
    }

    public static Map<String, String> getConfigs() throws IOException {
        if (!INIT_CONFIGS) {
            CONFIGS = getConfigsFromFile(Constants.RETRO_CONFIG_FILE);
            INIT_CONFIGS = true;
        }

        return CONFIGS;
    }

    public static Map<String, String> getConfigsFromFile(String filePath) throws IOException {
        Map<String, String> configs = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            line = line.trim();
            String[] map = line.split("=", 2);
            if (map.length != 2) {
                continue;
            }

            configs.put(map[0].trim(), map[1].trim().replace("\"", ""));
        }

        return configs;
    }

    public static Observable<Boolean> saveConfigs(final Map<String, String> configs) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(saveConfigsToPath(Constants.RETRO_CONFIG_FILE, configs));
            }
        });
    }

    public static boolean saveConfigsToPath(final String savePath,
                                            final Map<String, String> configs)
            throws IOException {

        if (configs == null) {
            return false;
        }

        FileWriter fileWriter = new FileWriter(savePath, false);
        fileWriter.write("# This file is auto generated\n\n");

        for (Map.Entry<String, String> row : configs.entrySet()) {
            fileWriter.write(formatRow(row.getKey(), row.getValue()));
        }

        fileWriter.flush();
        fileWriter.close();

        return true;
    }

    private static String formatRow(String key, String value) {
        return String.format("%s = \"%s\"\n", key, value);
    }
}
