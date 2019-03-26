package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean.Fanart;

import java.util.List;

import io.reactivex.Maybe;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by rany on 17-11-8.
 */

@Dao
public interface FanArtDao {
    @Insert(onConflict = REPLACE)
    void save(List<Fanart> fanarts);
    @Query("SELECT * FROM Fanart WHERE game = :game")
    Maybe<List<Fanart>> loadFanArts(String game);
}
