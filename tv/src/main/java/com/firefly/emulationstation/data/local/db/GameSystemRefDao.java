package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.firefly.emulationstation.data.bean.GameSystemRef;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

/**
 * Created by rany on 18-4-28.
 */

@Dao
public interface GameSystemRefDao {
    @Insert(onConflict = IGNORE)
    long insert(GameSystemRef ref);
    @Update
    int update(GameSystemRef ref);
    @Update
    int update(List<GameSystemRef> refs);
    @Query("SELECT * FROM GameSystemRef WHERE gameId = :gameId AND system = :system")
    GameSystemRef findOne(long gameId, String system);
    @Query("SELECT * FROM GameSystemRef WHERE gameId = :gameId")
    List<GameSystemRef> findByGameId(long gameId);
}
