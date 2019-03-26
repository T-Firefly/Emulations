package com.firefly.emulationstation.utils;

import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.gamerepo.data.GameRepoRepository;
import com.firefly.emulationstation.gamerepo.data.bean.Rom;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.utils.Utils.findRom;

/**
 * Created by rany on 18-5-14.
 */

public class RecommendedGameHelper {
    private GameRepository mGameRepository;
    private SystemsRepository mSystemsRepository;
    private GameRepoRepository mGameRepoRepository;

    @Inject
    public RecommendedGameHelper(GameRepository gameRepository,
                                 SystemsRepository systemsRepository,
                                 GameRepoRepository gameRepoRepository) {
        mGameRepository = gameRepository;
        mSystemsRepository = systemsRepository;
        mGameRepoRepository = gameRepoRepository;
    }

    public void run() {
        mGameRepoRepository.getRomsFromRepo("com.firefly")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<Rom>>() {
                    @Override
                    public void accept(List<Rom> roms) throws Exception {
                        processRoms(roms);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * If rom file already exists or is recommended save it to database.
     * Save exists rom in filesystem to avoid downloading it again,
     * and also use it's information.
     * @param roms roms which will be processed.
     * @throws Exception throw Exception when systems.xml is invalid.
     */
    private void processRoms(List<Rom> roms) throws Exception {
        List<GameSystem> gameSystems = mSystemsRepository
                .getDefaultGameSystem();
        if (gameSystems == null || gameSystems.isEmpty()) {
            return;
        }

        boolean romExists;
        String path;
        for (Rom rom : roms) {
            for (GameSystem gameSystem : gameSystems) {
                if (!gameSystem.getPlatformId().equals(rom.getSystem()) ||
                        !gameSystem.getExtension().contains(rom.getExt())) {
                    continue;
                }

                Game game = rom.toGame(gameSystem);
                path = game.getPath();
                romExists = false;

                if (new File(path).exists()) {
                    romExists = true;
                } else {
                    int index = path.lastIndexOf('.');
                    File possibleDir = new File(path.substring(0, index == -1 ? path.length() : index));
                     if (possibleDir.exists()) {
                         List<String> extensions = Arrays.asList(gameSystem.getExtensions());
                         String romPath = findRom(possibleDir, new HashSet<>(extensions));

                         if (romPath != null) {
                             romExists = true;
                             game.setPath(romPath);
                         }
                     }
                }

                if (romExists) {
                    game.setStatus(Game.STATUS_NORMAL);
                } else if (rom.isRecommended()) {
                    game.setStatus(Game.STATUS_RECOMMENDED);
                } else {
                    continue;
                }

                mGameRepository.saveGameWithSystem(game, gameSystem, false)
                        .subscribe(new Consumer<Game>() {
                            @Override
                            public void accept(Game game) throws Exception {

                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
            }
        }
    }
}
