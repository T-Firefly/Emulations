package com.firefly.emulationstation.commom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.BuildConfig;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Created by rany on 18-4-19.
 */

@GlideModule
public final class ESAppGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        if (BuildConfig.DEBUG) {
            builder.setLogLevel(Log.DEBUG);
        } else {
            builder.setLogLevel(Log.ERROR);
        }
    }
}
