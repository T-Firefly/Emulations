package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.GameSystemRef;
import com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean.Fanart;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameRomPath;

/**
 * Created by rany on 17-10-30.
 */

@Database(entities = {
            Game.class,
            GameRomPath.class,
            Fanart.class,
            DownloadInfo.class,
            GameSystemRef.class
        }, version = 6)
@TypeConverters({Converters.class})
public abstract class GameDatabase extends RoomDatabase {
    public abstract GameDao gameDao();
    public abstract GameRomPathDao gameRomPathDao();
    public abstract FanArtDao fanArtDao();
    public abstract DownloadInfoDao downloadInfoDao();
    public abstract GameSystemRefDao gameSystemRefDao();
}
