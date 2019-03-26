package com.firefly.emulationstation.gamerepo.data;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.local.db.GameDao;
import com.firefly.emulationstation.data.repository.DownloadRepository;
import com.firefly.emulationstation.gamerepo.category.CategoryPresenter;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;
import com.firefly.emulationstation.gamerepo.data.bean.Rom;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Created by rany on 18-4-24.
 */

@Singleton
public class GameRepoRepository implements IGameRepoSource {
    private GameRepoJsonSource mGameRepoJsonSource;
    private GameDao mGameDao;
    private DownloadRepository mDownloadRepository;

    @Inject
    GameRepoRepository(GameRepoJsonSource jsonSource,
                       GameDao gameDao,
                       DownloadRepository downloadRepository) {
        mGameRepoJsonSource = jsonSource;
        mGameDao = gameDao;
        mDownloadRepository = downloadRepository;
    }

    @Override
    public Observable<Set<Repo>> repos() {
        return mGameRepoJsonSource.repos();
    }

    @Override
    public Observable<Repo> addOrUpdateRepo(String path, boolean isNew) {
        return mGameRepoJsonSource.addOrUpdateRepo(path, isNew);
    }

    @Override
    public Observable<Set<Repo>> deleteRepo(Repo repo) {
        return mGameRepoJsonSource.deleteRepo(repo);
    }

    @Override
    public Observable<List<Rom>> getRoms(final GameSystem gameSystem, final int filterStatus) {
        return mGameRepoJsonSource.getRoms(gameSystem, filterStatus)
                .map(new Function<List<Rom>, List<Rom>>() {
                    @Override
                    public List<Rom> apply(List<Rom> roms) throws Exception {
                        Iterator<Rom> iterator = roms.iterator();
                        while (iterator.hasNext()) {
                            Rom rom = iterator.next();

                            if (rom == null) {
                                continue;
                            }

                            Game game = mGameDao.findOne(rom.getBasename(),
                                    String.format("%%%s%%", rom.getRepo()), gameSystem.getRomPathID());

                            if (filterStatus == CategoryPresenter.GAME_INSTALLED) {
                                if (game == null
                                        || (game.getStatus() != Game.STATUS_NORMAL
                                                && game.getStatus() != Game.STATUS_NEW_GAME)) {
                                    iterator.remove();
                                    continue;
                                }
                                
                                rom.setGame(game);
                            } else {
                                if (game != null
                                        && (game.getStatus() == Game.STATUS_NORMAL
                                                || game.getStatus() == Game.STATUS_NEW_GAME)) {
                                    iterator.remove();
                                    continue;
                                }

                                if (game != null) {
                                    DownloadInfo downloadInfo = mDownloadRepository
                                            .findOne(game.getDownloadId());
                                    game.setDownloadInfo(downloadInfo);
                                    rom.setGame(game);
                                }
                            }
                        }

                        return roms;
                    }
                });
    }

    @Override
    public Observable<List<Rom>> getRomsFromRepo(String repoId) {
        return mGameRepoJsonSource.getRomsFromRepo(repoId);
    }
}
