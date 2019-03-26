package com.firefly.emulationstation.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.firefly.emulationstation.commom.Constants;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by rany on 18-4-13.
 */

public class NetworkHelper {
    public static boolean isNetworkAvailable(Context context) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return false;
            }
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * If the url contain {@link Constants#POST_FLAG} it will be a post url,
     * this is our definition.
     * @param url url which want to check
     * @return true if is need request as post or false
     */
    public static boolean isPostUrl(String url) {
        return url.lastIndexOf(Constants.POST_FLAG) != -1;
    }

    public static String getPostUrl(String url) {
        if (isPostUrl(url)) {
            return url.substring(0, url.lastIndexOf(Constants.POST_FLAG));
        }

        return url;
    }

    public static RequestBody getPostData(String url) {
        if (isPostUrl(url)) {
            String param = url.substring(
                    url.lastIndexOf(Constants.POST_FLAG) + Constants.POST_FLAG.length());

            return RequestBody.create(
                    MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), param);
        }

        return null;
    }
}
