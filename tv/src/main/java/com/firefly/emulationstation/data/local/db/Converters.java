package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.firefly.emulationstation.data.bean.Dependency;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rany on 18-3-26.
 */

public final class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String dependenciesToJson(List<Dependency> dependencies) {
        if (dependencies == null || dependencies.isEmpty()) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        return gson.toJson(dependencies, new TypeToken<List<Dependency>>(){}.getType());
    }

    @TypeConverter
    public static List<Dependency> jsonToDependencies(String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, new TypeToken<List<Dependency>>(){}.getType());
    }

    @TypeConverter
    public static Map<String, String> jsonToMap(String json) {
        if (TextUtils.isEmpty(json)) {
            return new HashMap<>();
        }

        // Convert old format, if json is plaintext and contain ":" it will be
        // an error in this case, they can't be separated.
        if (!json.startsWith("{") && !json.endsWith("}") && json.contains(":")) {
            Map<String, String> result = new HashMap<>();
            String[] a = json.split("::");
            for (String b : a) {
                String[] c = b.split(":");
                if (c.length == 2) {
                    result.put(c[0], c[1]);
                }
            }

            return result;
        }

        Gson gson = new GsonBuilder().create();
        try {
            return gson.fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
        } catch (Exception ex) {
            Map<String, String> result = new HashMap<>();
            result.put("default", json);
            return result;
        }
    }

    @TypeConverter
    public static String mapToString(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Gson gson = new GsonBuilder().create();
        return gson.toJson(map, new TypeToken<Map<String, String>>() {}.getType());
    }
}