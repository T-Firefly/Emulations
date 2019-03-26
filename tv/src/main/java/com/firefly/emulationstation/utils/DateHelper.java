package com.firefly.emulationstation.utils;

import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rany on 17-10-31.
 */

public class DateHelper {
    private static SimpleDateFormat sdf =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    public static String dateToString(@NonNull Date date) {

        return sdf.format(date);
    }

    public static Date stringToDate(String str) {
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
