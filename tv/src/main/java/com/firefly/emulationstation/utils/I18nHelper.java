package com.firefly.emulationstation.utils;

import java.util.Locale;
import java.util.Map;

/**
 * Created by rany on 18-4-26.
 */

public class I18nHelper {
    public static String getValueFromMap(Map<String, String> map) {
        String region = Locale.getDefault().getCountry();
        String lang = Locale.getDefault().getLanguage();

        String key = lang + "-" + region;
        if (map.containsKey(key)) {
            return map.get(key);
        } else if (map.containsKey(lang)) {
            return map.get(lang);
        } else if (map.containsKey("default")) {
            return map.get("default");
        }

        return null;
    }

    public static String mapToString(Map<String, String> map) {
        if (map == null) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.append(entry.getKey());
            builder.append(':');
            builder.append(entry.getValue());
            builder.append("::");
        }

        builder.delete(builder.length() - 2, builder.length());

        return builder.toString();
    }
}
