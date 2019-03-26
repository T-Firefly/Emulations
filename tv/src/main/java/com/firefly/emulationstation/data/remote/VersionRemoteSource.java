package com.firefly.emulationstation.data.remote;

import com.firefly.emulationstation.BuildConfig;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.bean.Version;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Singleton
public class VersionRemoteSource {
    private GamePlay mRetroArch = null;
    private Version mVersion = null;

    @Inject
    VersionRemoteSource() {
    }

    public GamePlay getRetroArch(String url) {
        if (mRetroArch == null && url.endsWith(".json")) {
            try {
                Response response = requestUrl(url);

                Gson gson = new GsonBuilder().create();
                ResponseBody body = response.body();
                if (body != null) {
                    mRetroArch = gson.fromJson(body.string(), GamePlay.class);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mRetroArch;
    }

    public Version getNewVersion(boolean refresh) throws IOException {
        if (mVersion == null || refresh) {
            Response response = requestUrl(BuildConfig.VERSION_CHECK_URL);
            Gson gson = new GsonBuilder().create();
            ResponseBody body = response.body();
            String contentType = response.header("Content-Type");
            if ((contentType != null && contentType.contains("application/json"))
                    && body != null) {
                mVersion = gson.fromJson(body.string(), Version.class);
            }
        }

        return mVersion;
    }

    private Response requestUrl(String url) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            return null;
        }

        return response;
    }
}
