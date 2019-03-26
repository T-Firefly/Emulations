package com.firefly.emulationstation.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.bean.GameSystemRef;
import com.firefly.emulationstation.data.bean.StarGame;
import com.firefly.emulationstation.data.exceptions.ESException;
import com.firefly.emulationstation.data.exceptions.GameExistsException;
import com.firefly.emulationstation.data.local.ScanGame;
import com.firefly.emulationstation.data.local.db.DownloadInfoDao;
import com.firefly.emulationstation.data.local.db.GameDao;
import com.firefly.emulationstation.data.local.db.GameRomPathDao;
import com.firefly.emulationstation.data.local.db.GameSystemRefDao;
import com.firefly.emulationstation.data.remote.TheGamesDb.TheGamesDbSource;
import com.firefly.emulationstation.utils.ExternalStorageHelper;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.gamerepo.category.CategoryPresenter.GAME_NOT_INSTALL;

/**
 * Created by rany on 17-10-26.
 */

@Singleton
public class GameRepository {
    private Context mContext;
    private GameRomPathDao mGameRomPathDao;
    private GameDao mGameDao;
    private GameSystemRefDao mGameSystemRefDao;
    private DownloadInfoDao mDownloadInfoDao;
    private TheGamesDbSource mTheGamesDbSource;
    private SharedPreferences mSettings;
    private SystemsRepository mSystemsRepository;

    private Map<String, Maybe<List<Game>>> mScanning = new HashMap<>();

    @Inject
    public GameRepository(Context context,
                          SharedPreferences settings,
                          GameRomPathDao gameRomPathDao,
                          GameDao gameDao,
                          GameSystemRefDao gameSystemRefDao,
                          DownloadInfoDao downloadInfoDao,
                          TheGamesDbSource remoteSource,
                          SystemsRepository systemsRepository) {
        mContext = context;
        mSettings = settings;
        mGameRomPathDao = gameRomPathDao;
        mGameDao = gameDao;
        mGameSystemRefDao = gameSystemRefDao;
        mDownloadInfoDao = downloadInfoDao;
        mTheGamesDbSource = remoteSource;
        mSystemsRepository = systemsRepository;
    }


    public Maybe<List<Game>> getGameList(final GameSystem gameSystem, boolean refresh) {
        return getGameList(gameSystem, refresh, false);
    }

    public Maybe<List<Game>> getGameList(final GameSystem gameSystem,
                                         boolean refresh,
                                         final boolean getRecommend) {
        List<Maybe<List<Game>>> sources = new ArrayList<>();

        if (!refresh) {
            Maybe<List<Game>> dbSource = mGameDao.findAll(gameSystem.getName());
            sources.add(dbSource);
        }
        sources.add(scanGames(gameSystem));

        return Maybe.concat(sources)
                .filter(new Predicate<List<Game>>() {
                    @Override
                    public boolean test(List<Game> games) throws Exception {
                        return games != null && !games.isEmpty();
                    }
                })
                .firstElement()
                .toFlowable()
                .doOnNext(new Consumer<List<Game>>() {
                    @Override
                    public void accept(List<Game> games) throws Exception {
                        mScanning.remove(gameSystem.getName());

                        Iterator<Game> iterator = games.iterator();
                        while (iterator.hasNext()) {
                            Game game = iterator.next();

                            DownloadInfo downloadInfo = mDownloadInfoDao.findOne(game.getDownloadId());

                            if (!getRecommend && downloadInfo == null
                                    && game.getStatus() == Game.STATUS_RECOMMENDED) {
                                iterator.remove();
                                continue;
                            }

                            if (downloadInfo != null
                                    && downloadInfo.getStatus() != DownloadInfo.STATUS_COMPLETED) {
                                game.setDownloadInfo(downloadInfo);
                            }
                        }
                    }
                })
                .firstElement();
    }

    private Maybe<List<Game>> scanGames(final GameSystem gameSystem) {
        if (mScanning.containsKey(gameSystem.getName())) {
            return mScanning.get(gameSystem.getName());
        }

        ScanGame scanGame = new ScanGame(mContext, mGameDao, mGameRomPathDao, mGameSystemRefDao, mSettings);

        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String exts[] = gameSystem.getExtensions();
                String path = pathname.getPath().toLowerCase();

                if (pathname.isDirectory()) {
                    return true;
                }

                for (String ext : exts) {
                    if (path.endsWith(ext.toLowerCase())) {
                        return true;
                    }
                }

                return false;
            }
        };

        Maybe<List<Game>> scanObservable = scanGame.setFileFilter(fileFilter).scan(gameSystem);
        mScanning.put(gameSystem.getName(), scanObservable);

        return scanObservable;
    }

    public Maybe<List<Game>> searchGames(String keyword) {
        return mGameDao.searchGames(keyword);
    }

    public Maybe<List<StarGame>> loadStarGames() {
        return mGameDao.loadStarGames();
    }

    public Maybe<List<Game>> loadGamesFromSystems(String[] systems) {
        return mGameDao.loadGamesFromSystems(systems,
                new int[] {Game.STATUS_NEW_GAME, Game.STATUS_NORMAL});
    }

    public Maybe<List<Game>> loadGamesFromGameId(long[] ids) {
        return mGameDao.loadGamesFromId(ids,
                new int[] {Game.STATUS_NEW_GAME, Game.STATUS_NORMAL});
    }

    public Maybe<Integer> starCount() {
        return mGameDao.starCount();
    }

    public Flowable<Game> getGameDetail(final long id, final String platform) {
        return mGameDao.getGameDetail(id)
                .observeOn(Schedulers.io())
                .doOnNext(new Consumer<Game>() {
                    @Override
                    public void accept(Game game) throws Exception {
                        if (!game.isScraped()) {
                            getAndSaveRemoteGame(game, platform);
                        }
                    }
                });
    }

    private void getAndSaveRemoteGame(Game game, final String platform) {
        mTheGamesDbSource.getGameDetail(game, platform)
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Game>() {
                    @Override
                    public void accept(Game game) throws Exception {
                        Game localGame = mGameDao.findOne(game.getPath());
                        localGame.merge(game);

                        mGameDao.update(localGame);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
//        return Flowable.concat(mTheGamesDbSource.getGameDetail(game, platform),
//
//                ).subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.io());
    }

    public Observable<List<Game>> getGameDetailOptions(Game game, String platform) {
        return mTheGamesDbSource.getGameDetailOptions(game, platform);
    }

    public Observable<Boolean> updateGame(final Game game) {
        return Observable.create(
                new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        mGameDao.update(game);

                        e.onNext(true);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Observable<Game> saveGameWithSystem(final Game game, final GameSystem gameSystem, final boolean override) {
        return Observable.create(
                new ObservableOnSubscribe<Game>() {
                    @Override
                    public void subscribe(ObservableEmitter<Game> e) throws Exception {
                        Game eGame = mGameDao.findOne(game.getName(),
                                String.format("%%%s%%", game.getRepository()), game.getRomPathId());
                        long id = -1;

                        if (eGame != null) {
                            DownloadInfo downloadInfo =
                                    mDownloadInfoDao.findOne(eGame.getDownloadId());
                            game.setDownloadInfo(downloadInfo);
                            game.setDownloadId(eGame.getDownloadId());
                            if (downloadInfo != null) {
                                downloadInfo.setRef(game);
                            }

                            if (override || eGame.getStatus() == Game.STATUS_RECOMMENDED) {
                                id = eGame.getId();
                            } else {
                                game.setId(eGame.getId());
                                e.onError(new GameExistsException("The Game already exists.", game));
                                return;
                            }
                        } else {
                            id = mGameDao.save(game);
                        }

                        game.setId(id);
                        e.onNext(game);

                        GameSystemRef gameSystemRef = new GameSystemRef();
                        gameSystemRef.setGameId(id);
                        gameSystemRef.setSystem(gameSystem.getName());
                        mGameSystemRefDao.insert(gameSystemRef);
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Maybe<List<Long>> findCurrentDownloadRomBySystem(final GameSystem gameSystem) {
        return mGameDao.findCurrentDownloadRomBySystem(gameSystem.getName());
    }

    public Observable<Long> saveGameSystemRef(final GameSystemRef gameSystemRef) {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                e.onNext(mGameSystemRefDao.insert(gameSystemRef));
            }
        });
    }

    /**
     * Find game supports systems.
     * @param id The game id.
     * @return names of support system.
     */
    public List<String> findSupportSystems(long id) {
        return mGameDao.findSupportSystems(id);
    }

    /**
     * Delete a game from database. The file maybe not be deleted.
     * @param game Make sure the this param contain the valid id.
     * @return True if deleted or false.
     */
    public Observable<Boolean> deleteGame(final Game game) {
        return Observable
                .create(new ObservableOnSubscribe<Boolean>() {
                    @Override
                    public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                        if (mGameDao.delete(game) > 0) {
                            String path = game.getPath();

                            if (!TextUtils.isEmpty(game.getRepository())
                                    && path.contains(game.getRepository())) {
                                int index = path.indexOf(game.getRepository());
                                int stop = 2;

                                for (; index < path.length(); ++index) {
                                    if (path.charAt(index) == '/') {
                                        --stop;
                                    }

                                    if (stop == 0) {
                                        break;
                                    }
                                }

                                path = path.substring(0, index);
                            }

                            File file = new File(path);
                            if (file.exists() && file.canWrite()) {
                                ExternalStorageHelper.delete(file);
                            }

                            mDownloadInfoDao.deleteById(game.getDownloadId());
                            e.onNext(true);
                        } else {
                            e.onNext(false);
                        }
                    }
                });
    }

    /**
     * Star or unstar a game
     * @param game which Game will be stared or unstared.
     * @param gameSystem star a game with special GameSystem, if is null will star all of the
     *                   GameSystem which support this game.
     * @param star true if want to star a game, or false
     * @return the count of the GameSystemRef updated
     */
    public Observable<Long> star(final Game game, final GameSystem gameSystem, final boolean star) {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(ObservableEmitter<Long> e) throws Exception {
                long saved;
                String systemName = null;

                if (game instanceof StarGame) {
                    systemName = ((StarGame) game).getSystem();
                } else if (gameSystem != null) {
                    systemName = gameSystem.getName();
                }

                if (TextUtils.isEmpty(systemName)) {
                    List<GameSystemRef> gameSystemRefs =
                            mGameSystemRefDao.findByGameId(game.getId());
                    for (GameSystemRef gameSystemRef : gameSystemRefs) {
                        gameSystemRef.setStar(star);
                    }

                    saved = mGameSystemRefDao.update(gameSystemRefs);
                } else {
                    GameSystemRef gameSystemRef = mGameSystemRefDao.findOne(game.getId(), systemName);
                    gameSystemRef.setStar(star);
                    saved = mGameSystemRefDao.update(gameSystemRef);
                }


                if (saved > 0) {
                    game.setStar(star);
                }

                e.onNext(saved);
                e.onComplete();
            }
        });
    }
}
