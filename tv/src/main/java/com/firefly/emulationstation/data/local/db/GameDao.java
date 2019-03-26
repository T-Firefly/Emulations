package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.StarGame;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;
import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by rany on 17-10-30.
 */

@Dao
public interface GameDao {
    @Insert(onConflict = IGNORE)
    long save(Game game);
    @Insert(onConflict = IGNORE)
    long[] save(List<Game> game);
    @Update
    int update(Game game);
    @Update
    int update(List<Game> games);
    @Delete
    int delete(Game game);
    @Delete
    void delete(List<Game> games);
    @Query("SELECT * FROM game WHERE path = :path LIMIT 1")
    Game findOne(String path);

    /**
     * Find a Game by a downloadId, only for repo game.
     * @param downloadId DownloadInfo id
     * @return Matched game object or null.
     */
    @Query("SELECT * FROM Game WHERE downloadId = :downloadId")
    Game findByDownloadId(int downloadId);

    /**
     * Find a Game by name, path contain repo and romPathId.
     * @param name Game name
     * @param repo Name of repo.
     * @param romPathId romPathId
     * @return Matched game instance or null.
     */
    @Query("SELECT * FROM Game WHERE path LIKE :repo AND name = :name AND romPathId = :romPathId")
    Game findOne(String name, String repo, String romPathId);

    /**
     *  Find one game which from repo.
     * @param name Game name
     * @param repo The repository game belongs to
     * @param system Name of GameSystem
     * @return Matched game instance or null.
     */
    @Query("SELECT * FROM Game INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE name = :name AND repository = :repo AND GameSystemRef.system = :system")
    Game findOneFromRepo(String name, String repo, String system);
    @Query("SELECT * FROM game")
    List<Game> findAll();
    @Query("SELECT * FROM game  INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE GameSystemRef.system = :systemName ORDER BY name")
    List<Game> findAllFlat(String systemName);
    @Query("SELECT * FROM game INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE GameSystemRef.system = :system AND Game.status = :status " +
            "AND Game.repository = :repo ORDER BY name")
    List<Game> findAll(String repo, int status, String system);

    @Query("SELECT * FROM game WHERE id = :id")
    Flowable<Game> getGameDetail(long id);
    @Query("SELECT * FROM game INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE GameSystemRef.system = :systemName ORDER BY name")
    Maybe<List<Game>> findAll(String systemName);
    @Query("SELECT * from game WHERE (name LIKE :keyword OR displayNames LIKE :keyword) " +
            "AND status != " + Game.STATUS_RECOMMENDED)
    Maybe<List<Game>> searchGames(String keyword);
    @Query("SELECT DISTINCT * FROM game INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE GameSystemRef.isStar = 1")
    Maybe<List<StarGame>> loadStarGames();

    /**
     * Get games from database by systems
     * @param systems array of game system name
     * @param status array of status to match
     * @return matched games
     */
    @Query("SELECT * FROM game INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE GameSystemRef.system IN (:systems) AND Game.status IN (:status) " +
            " ORDER BY name")
    Maybe<List<Game>> loadGamesFromSystems(String[] systems, int[] status);

    /**
     * Get games from database by game id, not include the game
     * in {@link Game#STATUS_RECOMMENDED} status
     * @param ids array of game id
     * @param status array of status to match
     * @return matched games
     */
    @Query("SELECT * FROM game WHERE id IN (:ids) AND Game.status IN (:status) ORDER BY name")
    Maybe<List<Game>> loadGamesFromId(long[] ids, int[] status);

    @Query("SELECT DISTINCT COUNT(*) FROM game INNER JOIN GameSystemRef ON Game.id = GameSystemRef.gameId " +
            "WHERE GameSystemRef.isStar = 1")
    Maybe<Integer> starCount();

    /**
     * Find downloading game of system
     * @param systemName system name which want to find
     * @return Observable id list of game
     */
    @Query("SELECT Game.id FROM Game INNER JOIN GameSystemRef " +
            "ON Game.id = GameSystemRef.gameId " +
            "INNER JOIN DownloadInfo ON Game.downloadId = DownloadInfo.id " +
            "WHERE GameSystemRef.system = :systemName AND DownloadInfo.status = 2")
    Maybe<List<Long>> findCurrentDownloadRomBySystem(String systemName);

    /**
     * Find the Game support system
     * @param id id of game
     * @return support system name list
     */
    @Query("SELECT GameSystemRef.system FROM Game INNER JOIN GameSystemRef " +
            "ON Game.id = GameSystemRef.gameId WHERE Game.id = :id")
    List<String> findSupportSystems(long id);
}
