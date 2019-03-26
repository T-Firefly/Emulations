package com.firefly.emulationstation.gamerepo.repogames;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.IBinder;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.fragment.MenuDialog;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystemRef;
import com.firefly.emulationstation.data.exceptions.GameExistsException;
import com.firefly.emulationstation.data.exceptions.UrlInvalidException;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.di.ActivityScoped;
import com.firefly.emulationstation.gamerepo.data.GameRepoRepository;
import com.firefly.emulationstation.gamerepo.data.bean.Category;
import com.firefly.emulationstation.gamerepo.data.bean.Rom;
import com.firefly.emulationstation.services.downloader.DownloadService;
import com.firefly.emulationstation.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.gamerepo.category.CategoryPresenter.GAME_NOT_INSTALL;

/**
 * Created by rany on 18-4-23.
 */

@ActivityScoped
public class RepoGamesPresenter
        implements RepoGamesContract.Presenter, DownloadService.DownloadListener {
    private RepoGamesContract.View mView;

    private Context mContext;
    private GameRepoRepository mGameRepoRepository;
    private GameRepository mGameRepository;
    private Map<Game, Rom> mRomsMap = new LinkedHashMap<>();
    private Set<Long> mCurrentDownload = new HashSet<>();

    private Category mCurrentCategory;
    private int mInstallStatus = GAME_NOT_INSTALL;

    private DownloadService mDownloadService;
    private ServiceConnection mServiceConnection = new DownloadServiceConnection();

    /**
     * This object is may not from database so getDownloadId() may return 0
     */
    private Game mSelectedGame;

    @Inject
    RepoGamesPresenter(GameRepoRepository gameRepoRepository,
                       GameRepository gameRepository,
                       Context context) {
        mGameRepoRepository = gameRepoRepository;
        mGameRepository = gameRepository;
        mContext = context;
    }

    @Override
    public void subscribe(RepoGamesContract.View view) {
        mView = view;

        mContext.bindService(
                new Intent(mContext, DownloadService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void unsubscribe() {
        mDownloadService.unregisterListener(this);
        mContext.unbindService(mServiceConnection);
        mView = null;
    }

    @Override
    public void onCategorySelectChanged(Category category) {
        if (category != null && category.hasSystem()) {
            if (mView != null) {
                mView.setLoading(true);
            }
            mCurrentCategory = category;

            mGameRepoRepository.getRoms(category.gameSystem, mInstallStatus)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Rom>>() {
                        @Override
                        public void accept(List<Rom> roms) throws Exception {
                            List<Game> games = new ArrayList<>(roms.size());

                            mRomsMap.clear();
                            for (Rom rom : roms) {
                                Game game = rom.toGame(mCurrentCategory.gameSystem);

                                mRomsMap.put(game, rom);
                                games.add(game);
                            }

                            mView.showGames(games);
                            mView.setLoading(false);
                            if (games.isEmpty()) {
                                mView.showEmptyView(true);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });

            mGameRepository.findCurrentDownloadRomBySystem(mCurrentCategory.gameSystem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<Long>>() {
                        @Override
                        public void accept(List<Long> longs) throws Exception {
                            mCurrentDownload.clear();
                            mCurrentDownload.addAll(longs);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void setInstallStatus(int status) {
        mInstallStatus = status;

        onCategorySelectChanged(mCurrentCategory);
    }

    @Override
    public void onGameSelected(Game game, boolean ok) {
        mSelectedGame = game;

        if (mInstallStatus == GAME_NOT_INSTALL
                // this condition is for the moment of Game download completed
                && game.getStatus() != Game.STATUS_NEW_GAME) {
            DownloadInfo info = game.getDownloadInfo();
            // If info is not null, that is the Game info is exist in database
            if (info != null && info.getStatus() != DownloadInfo.STATUS_COMPLETED) {
                List<MenuDialog.MenuItem> menuItems = new ArrayList<>();
                TypedArray menus = null;

                switch (info.getStatus()) {
                    case DownloadInfo.STATUS_DOWNLOADING:
                    case DownloadInfo.STATUS_PENDING:
                        menus = mContext.getResources()
                                .obtainTypedArray(R.array.repogame_downloading_status_menu);
                        break;
                    case DownloadInfo.STATUS_ERROR:
                        menus = mContext.getResources()
                                .obtainTypedArray(R.array.retrogame_error_status_menu);
                        break;
                    case DownloadInfo.STATUS_PAUSE:
                        menus = mContext.getResources()
                                .obtainTypedArray(R.array.retrogame_error_status_menu);
                        break;
                    case DownloadInfo.STATUS_STOP:
                        menus = mContext.getResources()
                                .obtainTypedArray(R.array.retrogame_cancel_status_menu);
                        break;
                    case DownloadInfo.STATUS_COMPLETED:
                        break;
                    case DownloadInfo.STATUS_AUTO_START:
                        break;
                }

                if (menus != null) {
                    for (int i = 0; i < menus.length(); ++i) {
                        int resId = menus.getResourceId(i, R.string.default_menu_item);
                        menuItems.add(new MenuDialog.MenuItem(resId));
                    }

                    menus.recycle();
                    mView.showDownloadManagerMenu(menuItems);
                    return;
                }
            }

            if (ok) {
                downloadGame(game, false);
            } else {
                mView.showInstallNoticeView(game);
            }
        } else {
            if (ok) {
                removeGame(game);
            } else {
                mView.showRemoveNoticeView(game);
            }
        }
    }

    @Override
    public void onMenuSelected(MenuDialog.MenuItem menuItem) {
        final DownloadInfo info = mSelectedGame.getDownloadInfo();

        switch (menuItem.getId()) {
            case R.string.menu_item_pause:
                runOnNewThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                mDownloadService.pause(info.getId());
                            }
                        });
                break;
            case R.string.menu_item_cancel:
                runOnNewThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                mDownloadService.stop(info.getId());
                            }
                        });
                break;
            case R.string.menu_item_restart:
                runOnNewThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mDownloadService.download(mSelectedGame.getDownloadInfo());
                                    mCurrentDownload.add(mSelectedGame.getId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } catch (UrlInvalidException e) {
                                    Utils.showToast(mContext, R.string.url_is_invalid);
                                }
                            }
                        });
                break;
            case R.string.menu_item_delete:
                runOnNewThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadService.stop(info.getId());
                        mGameRepository.deleteGame(mSelectedGame)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) throws Exception {
                                        if (aBoolean) {
                                            // refresh
                                            onCategorySelectChanged(mCurrentCategory);
                                        } else {
                                            mView.showError(mContext.getString(R.string.delete_failed));
                                        }
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        throwable.printStackTrace();
                                        mView.showError(mContext.getString(R.string.delete_failed));
                                    }
                                });
                    }
                });
                break;
        }
    }

    @Override
    public void progress(DownloadInfo info) {
        // filter info
        if (info.getType() != DownloadInfo.TYPE_ROM
                || !(info.getRef() instanceof Game)
                || !isCurrentSystemDownload((Game) info.getRef())
                || (mCurrentCategory != null && !mCurrentCategory.hasSystem())) {
            return;
        }

        mView.updateDownloadProgress(info);
    }

    @Override
    public void completed(DownloadInfo info) {
        Game game = (Game) info.getRef();;
        if (info.getType() != DownloadInfo.TYPE_ROM) {
            return;
        }

        if (game != null) {
            // Not saving at here, save at RomDownloadReceiver
            game.setStatus(Game.STATUS_NEW_GAME);
        }
    }

    private void removeGame(final Game game) {
        mGameRepository.deleteGame(game)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        String msg;
                        if (aBoolean) {
                            msg = mContext.getString(R.string.game_removed, game.getDisplayName());
                        } else {
                            msg = mContext.getString(R.string.delete_failed);
                        }

                        mView.showError(msg);
                        // refresh list
                        setInstallStatus(mInstallStatus);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void downloadGame(final Game game, boolean override) {
        final Rom rom = mRomsMap.get(game);

        mGameRepository.saveGameWithSystem(game, mCurrentCategory.gameSystem, override)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Game>() {
                    @Override
                    public void accept(Game savedGame) throws Exception {
                        final DownloadInfo downloadInfo = new DownloadInfo(
                                game.getName(),
                                rom.getUrl(),
                                DownloadInfo.TYPE_ROM,
                                game.getPath(),
                                rom.getVersion());

                        startDownloadGame(downloadInfo, savedGame);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (throwable instanceof GameExistsException) {
                            Game savedGame = ((GameExistsException) throwable).getGame();

                            processGameExistsException(savedGame);
                        }
                    }
                });
    }

    private void startDownloadGame(DownloadInfo downloadInfo, Game game) {
        downloadInfo.setRef(game);
        // set progress to 0 to make the progress view show in ImageCardView
        game.setProgress(0);
        mCurrentDownload.add(game.getId());

        try {
            int id = mDownloadService.download(downloadInfo);
            game.setDownloadId(id);
            mGameRepository.updateGame(game)
                    .subscribeOn(Schedulers.io())
                    .subscribe();
            Rom rom = mRomsMap.get(game);

            Utils.downloadGameDependencies(rom.getDependencies(),
                    mDownloadService, downloadInfo.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        } catch (UrlInvalidException e) {
            Utils.showToast(mContext, R.string.url_is_invalid);
        }
    }

    private void processGameExistsException(Game game) {
        if (game.getDownloadInfo() != null) {
            DownloadInfo downloadInfo = game.getDownloadInfo();
            Rom rom = mRomsMap.get(game);
            boolean sameVersion = downloadInfo.getVersion().equals(rom.getVersion());

            if (!sameVersion) {
                downloadInfo.setVersion(rom.getVersion());
                downloadInfo.setUrl(rom.getUrl());
                downloadInfo.setProgress(0);
            }

            switch (downloadInfo.getStatus()) {
                case DownloadInfo.STATUS_AUTO_START:
                    break;
                case DownloadInfo.STATUS_COMPLETED:
                case DownloadInfo.STATUS_DOWNLOADING:
                case DownloadInfo.STATUS_ERROR:
                case DownloadInfo.STATUS_PENDING:
                case DownloadInfo.STATUS_PAUSE:
                case DownloadInfo.STATUS_STOP:
                    progress(downloadInfo);

                    saveGameSystemRef(game);
                    mCurrentDownload.add(game.getId());

                    try {
                        mDownloadService.download(downloadInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } catch (UrlInvalidException e) {
                        Utils.showToast(mContext, R.string.url_is_invalid);
                    }
                    break;
            }
        } else {
            mView.showGameExistsView(game);
        }
    }

    private void saveGameSystemRef(Game game) {
        GameSystemRef gameSystemRef = new GameSystemRef();
        gameSystemRef.setGameId(game.getId());
        gameSystemRef.setSystem(mCurrentCategory.gameSystem.getName());
        mGameRepository.saveGameSystemRef(gameSystemRef)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe();

    }

    private boolean isCurrentSystemDownload(Game game) {
        return mCurrentDownload.contains(game.getId());
    }

    private void runOnNewThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    private class DownloadServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadService = ((DownloadService.DownloadBinder) service).getService();

            mDownloadService.registerListener(RepoGamesPresenter.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadService.unregisterListener(RepoGamesPresenter.this);
            mDownloadService = null;
        }
    }
}
