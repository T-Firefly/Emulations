package com.firefly.emulationstation.utils;

import android.content.SharedPreferences;
import android.database.Cursor;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameRomPath;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.bean.GameSystemRef;
import com.firefly.emulationstation.data.local.db.GameDao;
import com.firefly.emulationstation.data.local.db.GameDatabase;
import com.firefly.emulationstation.data.local.db.GameRomPathDao;
import com.firefly.emulationstation.data.local.db.GameSystemRefDao;
import com.firefly.emulationstation.data.repository.SystemsRepository;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by rany on 18-3-19.
 */

public class DbVersionUpgradeHelper {
    private SharedPreferences mSettings;
    private GameDatabase mDB;
    private SystemsRepository mSystemsRepository;

    @Inject
    public DbVersionUpgradeHelper(SharedPreferences settings,
                                  GameDatabase db,
                                  SystemsRepository systemsRepository) {
        mSettings = settings;
        mDB = db;
        mSystemsRepository = systemsRepository;
    }

    public void update() {
        // This number come from Migration_X_X.java
        // in com.firefly.emulationstation.data.local.db.migration
        int v = mSettings.getInt(Constants.SETTINGS_DB_UPDATE_VERSION, -1);

        switch (v) {
            case 1:
                upgradeTo4();
            case 2:
                upgradeTo6();
                break;
            default:
                break;
        }

        // Reset flag
        mSettings.edit()
                .putInt(Constants.SETTINGS_DB_UPDATE_VERSION, -1)
                .apply();
    }

    private void upgradeTo4() {
        GameRomPathDao RPdo = mDB.gameRomPathDao();
        GameDao gdo = mDB.gameDao();

        List<GameRomPath> romPaths = RPdo.findAll();
        for (GameRomPath romPath : romPaths) {
            GameSystem system = mSystemsRepository.getGameSystem(romPath.getRomPathId());
            if (system != null) {
                romPath.setRomPathId(system.getRomPathID());
            }
        }
        RPdo.update(romPaths);

        List<Game> games = gdo.findAll();
        for (Game game : games) {
            // only true when upgrade database version from 3 to 4
            GameSystem system = mSystemsRepository.getGameSystem(game.getPlatformId());
            if (system != null) {
                game.setPlatformId(system.getPlatformId());
                game.setExt(Utils.getFileExt(game.getPath()));
            }
        }
        gdo.update(games);
    }

    private void upgradeTo6() {
        GameDao gameDao = mDB.gameDao();
        GameSystemRefDao gameSystemRefDao = mDB.gameSystemRefDao();

        List<Game> games = gameDao.findAll();
        for (Game game : games) {
            List<GameSystem> gameSystems = mSystemsRepository.getMatchGameSystems(
                    game.getRomPathId(),
                    game.getExt());
            for (GameSystem gameSystem : gameSystems) {
                GameSystemRef gameSystemRef = new GameSystemRef();
                gameSystemRef.setGameId(game.getId());
                gameSystemRef.setStar(game.isStar());
                gameSystemRef.setSystem(gameSystem.getName());

                gameSystemRefDao.insert(gameSystemRef);
            }
        }
    }
}
