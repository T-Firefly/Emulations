package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.firefly.emulationstation.data.bean.GameRomPath;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * Created by rany on 17-10-31.
 */

@Dao
public interface GameRomPathDao {
    @Query("SELECT * FROM gamerompath")
    List<GameRomPath> findAll();
    @Query("SELECT * FROM gamerompath WHERE romPathId = :id")
    List<GameRomPath> findAllByRomPathId(String id);
    @Insert(onConflict = IGNORE)
    void save(GameRomPath romPath);
    @Update
    void update(List<GameRomPath> romPaths);
    @Update
    void update(GameRomPath romPath);
}
