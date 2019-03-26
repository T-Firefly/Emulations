package com.firefly.emulationstation.di;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.firefly.emulationstation.data.local.db.DownloadInfoDao;
import com.firefly.emulationstation.data.local.db.GameDao;
import com.firefly.emulationstation.data.local.db.GameDatabase;
import com.firefly.emulationstation.data.local.db.GameRomPathDao;
import com.firefly.emulationstation.data.local.db.GameSystemRefDao;
import com.firefly.emulationstation.data.local.db.migration.Migration_1_2;
import com.firefly.emulationstation.data.local.db.migration.Migration_2_3;
import com.firefly.emulationstation.data.local.db.migration.Migration_3_4;
import com.firefly.emulationstation.data.local.db.migration.Migration_4_5;
import com.firefly.emulationstation.data.local.db.migration.Migration_5_6;
import com.firefly.emulationstation.data.local.db.migration.Migration_6_7;
import com.firefly.emulationstation.data.repository.SystemsRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rany on 17-10-28.
 */

@Module
public class DataModule {
    @Singleton
    @Provides
    GameDatabase provideDb(Context context, SharedPreferences settings) {
        return Room.databaseBuilder(context.getApplicationContext(), GameDatabase.class,"gamedb.db")
                .addMigrations(
                        new Migration_1_2(),
                        new Migration_2_3(),
                        new Migration_3_4(settings),
                        new Migration_4_5(),
                        new Migration_5_6(settings),
                        new Migration_6_7()
                )
                .build();
    }

    @Singleton
    @Provides
    GameDao provideGameDao(GameDatabase db) {
        return db.gameDao();
    }

    @Singleton
    @Provides
    GameRomPathDao provideGameRomPath(GameDatabase db) {
        return db.gameRomPathDao();
    }

    @Singleton
    @Provides
    DownloadInfoDao provideDownloadInfo(GameDatabase db) {
        return db.downloadInfoDao();
    }

    @Singleton
    @Provides
    GameSystemRefDao provideGameSystemRef(GameDatabase db) {
        return db.gameSystemRefDao();
    }
}
