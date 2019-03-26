package com.firefly.emulationstation.scraper;

import android.content.Context;
import android.os.Handler;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.utils.NetworkHelper;
import com.firefly.emulationstation.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rany on 17-12-18.
 */

public class ScraperPresenter implements ScraperContract.Presenter {
    private String[] mGameSystemNames;
    private long[] mGameIds;
    private boolean mAutoSelect;

    /**
     * Games waiting to scrape
     */
    private List<Game> mGames;
    private int index = 0;
    /**
     * Game info for select
     */
    private List<Game> mOptions;
    private Game mSelectedGameInfo;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Inject
    GameRepository mGameRepository;
    @Inject
    SystemsRepository mSystemsRepository;

    private ScraperContract.View mView;
    private Context mContext;

    private MaybeObserver<List<Game>> mGamesObserver = new MaybeObserver<List<Game>>() {
        @Override
        public void onSubscribe(Disposable d) {
            mDisposables.add(d);
        }

        @Override
        public void onSuccess(List<Game> games) {
            mGames = games;
            fetchGameInfoOptions();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            Utils.showToast(mContext, R.string.load_games_failed);
            mView.finish();
        }

        @Override
        public void onComplete() {
            Utils.showToast(mContext, R.string.no_games_for_scraper);
            mView.finish();
        }
    };

    @Inject
    public ScraperPresenter(Context context) {
        mContext = context;
    }

    @Override
    public void subscribe(ScraperContract.View view) {
        mView = view;

        if (!NetworkHelper.isNetworkAvailable(mContext)) {
            Utils.showToast(mContext, R.string.no_internet);
            mView.finish();
            return;
        }

        if (mGameSystemNames != null) {
            loadGamesFromSystems();
        } else {
            loadGamesFromGameNames();
        }
    }

    @Override
    public void unsubscribe() {
        mDisposables.clear();
    }

    private void loadGamesFromGameNames() {
        mGameRepository.loadGamesFromGameId(mGameIds)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(mGamesObserver);
    }

    private void loadGamesFromSystems() {
        mGameRepository.loadGamesFromSystems(mGameSystemNames)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(mGamesObserver);
    }

    @Override
    public void nextGame() {
        if (mGames == null || mGames.size() <= index + 1) {
            mView.finish();
            return;
        }
        ++index;
        mView.showProgressBar(mGames.get(index).getName());
        fetchGameInfoOptions();
    }

    @Override
    public void initData(long[] ids, String[] systemNames, boolean autoSelect) {
        mGameIds = ids;
        mGameSystemNames = systemNames;
        mAutoSelect = autoSelect;
    }

    @Override
    public void fetchGameInfoOptions() {
        if (mGames == null || mGames.isEmpty()) {
            mView.finish();
            return;
        }

        Game game = mGames.get(index);

        Disposable disposable = mGameRepository
                .getGameDetailOptions(game, GameSystem.getPlatformName(game.getPlatformId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Game>>() {
                    @Override
                    public void accept(List<Game> games) throws Exception {
                        mOptions = games;

                        mView.showOptionsGameInfo(games);

                        if (mAutoSelect) {
                            doNext();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                        if (mAutoSelect) {
                            nextGame();
                        } else {
                            Utils.showToast(mContext, R.string.no_game_info);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mView.finish();
                                }
                            }, 500);
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        if (!mAutoSelect) {
                            mView.hideProgressBar();
                        }
                    }
                });

        mDisposables.add(disposable);
    }

    private void doNext() {
        mSelectedGameInfo = mOptions.get(0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                saveGameInfo();
            }
        }, 1000);
    }

    @Override
    public void saveGameInfo() {
        Game game = mGames.get(index);

        game.merge(mSelectedGameInfo);
        if (game.getDisplayName("default") == null) {
            game.setDisplayName("default", mSelectedGameInfo.getName());
        } else {
            game.setDisplayName("en", mSelectedGameInfo.getName());
        }

        Disposable disposable = mGameRepository.updateGame(game)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            nextGame();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

        mDisposables.add(disposable);
    }

    @Override
    public Game getSelectedGameInfo() {
        return mSelectedGameInfo;
    }

    @Override
    public Game getSelectedGameInfo(int index) {
        return mOptions.get(index);
    }

    @Override
    public void setSelectedGameInfo(int index) {
        mSelectedGameInfo = mOptions.get(index);
    }

    @Override
    public boolean isAutoSelect() {
        return mAutoSelect;
    }

    @Override
    public void clearGameInfo() {
        final Game game = mGames.get(index);
        game.setCardImageUrl(null);
        game.setBackgroundImageUrl(null);
        game.setDescription(null);
        game.setRating(0);

        Disposable disposable = mGameRepository.updateGame(game)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            mView.finish();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

        mDisposables.add(disposable);
    }
}
