package com.firefly.emulationstation.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v17.leanback.app.ProgressBarManager;

import com.firefly.emulationstation.data.remote.TheGamesDb.legacy.service.TheGamesDbLegacyService;
import com.firefly.emulationstation.data.remote.TheGamesDb.service.TheGamesDbService;
import com.firefly.emulationstation.utils.RetrofitBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rany on 17-10-25.
 */

@Module
public class AppModule {
    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    Context provideContext() {
        return mApplication;
    }

    @Singleton
    @Provides
    TheGamesDbLegacyService provideRetrofit() {
        return new RetrofitBuilder(RetrofitBuilder.CONVERTER_SIMPLE_XML, false)
                .create(TheGamesDbLegacyService.class, "http://legacy.thegamesdb.net/api/");
    }

    @Singleton
    @Provides
    TheGamesDbService provideTheGamesDbService() {
        return new RetrofitBuilder(RetrofitBuilder.CONVERTER_GSON, true)
                .create(TheGamesDbService.class, "https://api.thegamesdb.net");
    }

    @Provides
    ProgressBarManager provideProgressBarManager() {
        return new ProgressBarManager();
    }

    @Provides
    SharedPreferences provideDefaultSharedPreference() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication.getApplicationContext());
    }
}
