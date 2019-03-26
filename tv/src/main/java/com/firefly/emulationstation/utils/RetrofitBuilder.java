package com.firefly.emulationstation.utils;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.firefly.emulationstation.BuildConfig;
import com.firefly.emulationstation.commom.Constants;

import java.io.IOException;

import javax.inject.Singleton;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Created by rany on 17-10-16.
 */

@Singleton
public class RetrofitBuilder {
    @IntDef({CONVERTER_GSON, CONVERTER_SIMPLE_XML})
    public @interface Converter {};

    public static final int CONVERTER_GSON = 0;
    public static final int CONVERTER_SIMPLE_XML = 1;

    private Retrofit.Builder mBuilder;

    public RetrofitBuilder(@Converter int converter, boolean needAuth) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        if (BuildConfig.DEBUG) {
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            interceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        OkHttpClient.Builder builder = new OkHttpClient
                .Builder()
                .addInterceptor(interceptor);

        if (needAuth) {
            builder.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(@NonNull Chain chain) throws IOException {
                    Request request = chain.request();

                    HttpUrl url = request.url()
                            .newBuilder()
                            .addQueryParameter("apikey", Constants.THE_GAMES_DB_APPKEY)
                            .build();
                    Request realReq = request.newBuilder().url(url).build();

                    return chain.proceed(realReq);
                }
            });
        }

        mBuilder = new Retrofit.Builder();
        switch (converter) {
            case CONVERTER_GSON:
                mBuilder.addConverterFactory(GsonConverterFactory.create());
                break;
            case CONVERTER_SIMPLE_XML:
                mBuilder.addConverterFactory(SimpleXmlConverterFactory.create());
                break;
        }
        mBuilder.client(builder.build());
    }

    public <T> T create(final Class<T> service, String url) {
        return mBuilder.baseUrl(url)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(service);
    }
}
