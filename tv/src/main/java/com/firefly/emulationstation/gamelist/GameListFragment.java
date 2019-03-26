package com.firefly.emulationstation.gamelist;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.ProgressBarManager;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.GlideApp;
import com.firefly.emulationstation.commom.IKeyDownEvent;
import com.firefly.emulationstation.commom.fragment.GridFragment;
import com.firefly.emulationstation.commom.fragment.MenuDialog;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.commom.presenter.CardPresenter;
import com.firefly.emulationstation.commom.presenter.GameGridPresenter;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.exceptions.UrlInvalidException;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.gamedetail.DetailsActivity;
import com.firefly.emulationstation.gamerepo.RepoActivity;
import com.firefly.emulationstation.gamerepo.repogames.RomActionNoticeDialog;
import com.firefly.emulationstation.scraper.ScraperActivity;
import com.firefly.emulationstation.services.downloader.DownloadService;
import com.firefly.emulationstation.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.MaybeObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.commom.Constants.SETTINGS_SHOW_RECOMMENDED_GAME;

/**
 * Game list for a emulator {@link GridFragment} subclass.
 */
public class GameListFragment extends GridFragment
        implements IKeyDownEvent, DownloadService.DownloadListener,
        MenuDialog.OnMenuItemClickListener {
    private static final String TAG = GameListFragment.class.getSimpleName();
    private static final int REQUEST_GAME_DETAIL = 1;
    private static final int REQUEST_SCRAPER = 2;

    private static final int COL_NUM = 6;
    private static final int BACKGROUND_UPDATE_DELAY = 500;

    private final Handler mHandler = new Handler();
    private BackgroundManager mBackgroundManager;
    private Timer mBackgroundTimer;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;

    private CompositeDisposable mDisposables = new CompositeDisposable();
    private EmptyViewManager mEmptyViewManager = new EmptyViewManager();

    @Inject
    GameRepository mGameRepository;
    @Inject
    SystemsRepository mSystemsRepository;
    @Inject
    ProgressBarManager mProgressBarManager;
    @Inject
    SharedPreferences mSettings;

    private final ArrayObjectAdapter mRowsAdapter;
    /**
     * If show star games mGameSystem will be null
     */
    private GameSystem mGameSystem;
    private Game mSelectGame;
    private FrameLayout mRootView;
    private View mAnimateView;
    private boolean isScanning = false;

    private boolean isScanningOrRefresh = true;

    private DownloadService mDownloadService;
    private ServiceConnection mServiceConnection = new DownloadServiceConnection();

    final private MaybeObserver<List<? extends Game>> mGamesObserver =
            new MaybeObserver<List<? extends Game>>() {
        @Override
        public void onSubscribe(Disposable d) {
            mDisposables.add(d);
        }

        @Override
        public void onSuccess(List<? extends Game> games) {
            mRowsAdapter.clear();
            mProgressBarManager.hide();

            if (games.isEmpty()) {
                showEmptyView();
            } else {
                mEmptyViewManager.hide();
                mRowsAdapter.addAll(0, games);
            }

            scanCompleted();
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
            scanCompleted();
        }

        @Override
        public void onComplete() {
            showEmptyView();
            mRowsAdapter.clear();
            scanCompleted();

            mProgressBarManager.hide();
        }

        private void showEmptyView() {
            mEmptyViewManager.setRootView(mRootView);
            mEmptyViewManager.show(mGameSystem);
        }
    };

    public GameListFragment() {
        GameGridPresenter gameGridPresenter = new GameGridPresenter();
        gameGridPresenter.setNumberOfColumns(COL_NUM);
        setGridPresenter(gameGridPresenter);

        CardPresenter cardPresenter = new CardPresenter();
        mRowsAdapter = new ArrayObjectAdapter(cardPresenter);
        setAdapter(mRowsAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mGameSystem = (GameSystem) args.getSerializable("system");
        }

        setEventListener();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRootView = (FrameLayout)view.findViewById(R.id.browse_grid_dock);
        prepareBackgroundManager();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isScanningOrRefresh) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    createRows(false);
                    getMainFragmentAdapter()
                            .getFragmentHost()
                            .notifyDataReady(getMainFragmentAdapter());
                }
            }, 100);

            mBackgroundManager.setDrawable(mDefaultBackground);
            isScanningOrRefresh = false;
        }

        getActivity().bindService(
                new Intent(getActivity(), DownloadService.class),
                mServiceConnection, Context.BIND_AUTO_CREATE);

        mEmptyViewManager.show();
    }

    @Override
    public void onPause() {
        super.onPause();

        mDisposables.clear();

        mEmptyViewManager.hide();

        if (mProgressBarManager != null) {
            mProgressBarManager.hide();
        }

        if (null != mBackgroundTimer) {
            Log.d(TAG, "onDestroy: " + mBackgroundTimer.toString());
            mBackgroundTimer.cancel();
        }
        mBackgroundManager.setDrawable(mDefaultBackground);

        if (mDownloadService != null) {
            getActivity().unbindService(mServiceConnection);
            mDownloadService.unregisterListener(this);
        }
    }

    private void setEventListener() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
        mEmptyViewManager.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isScanningOrRefresh = true;
            }
        });
    }

    private void showGameDetail(View aniView, Game game, GameSystem gameSystem) {
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra(DetailsActivity.GAME, game);
        intent.putExtra(DetailsActivity.SYSTEM, gameSystem);

        Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                getActivity(),
                aniView,
                DetailsActivity.SHARED_ELEMENT_NAME).toBundle();

        startActivityForResult(intent, REQUEST_GAME_DETAIL, bundle);
    }

    private void showMenuDialog(int menuArrayResId) {
        MenuDialog dialog = new MenuDialog();
        dialog.setListener(this);
        dialog.setMenuItem(getResources(), menuArrayResId);
        dialog.show(getFragmentManager(), "menu");
    }

    private void showProgressBar() {
        mProgressBarManager.setRootView(mRootView);
        mProgressBarManager.setInitialDelay(100);
        mProgressBarManager.show();
        isScanning = true;
    }

    /**
     * @param refresh use for rescan the game rom
     */
    private void createRows(boolean refresh) {
        showProgressBar();

        if (mGameSystem == null) {
            loadStarGame();
        } else {
            loadSystemGame(refresh);
        }
    }

    private void loadStarGame() {
         mGameRepository.loadStarGames()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(mGamesObserver);

         isScanningOrRefresh = true;
    }

    private void loadSystemGame(boolean refresh) {
        final boolean showRecommended = mSettings.getBoolean(SETTINGS_SHOW_RECOMMENDED_GAME, true);
        mGameRepository.getGameList(mGameSystem, refresh, showRecommended)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(mGamesObserver);

        isScanningOrRefresh = true;
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        if (!mBackgroundManager.isAttached()) {
            mBackgroundManager.attach(getActivity().getWindow());
        }
        mDefaultBackground = getResources().getDrawable(R.color.default_browse_fragment_bg);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(String uri) {
        int width = mMetrics.widthPixels;
        int height = mMetrics.heightPixels;
        Activity activity = getActivity();

        if (activity != null) {
            GlideApp.with(activity)
                    .load(uri)
                    .centerCrop()
                    .error(mDefaultBackground)
                    .into(new SimpleTarget<Drawable>(width, height) {
                        @Override
                        public void onResourceReady(Drawable resource,
                                                    @Nullable Transition transition) {
                            mBackgroundManager.setDrawable(resource);
                        }
                    });
        }
    }

    private void startBackgroundTimer(String url) {
        if (url == null) {
            return;
        }
        synchronized (this) {
            if (null != mBackgroundTimer) {
                mBackgroundTimer.cancel();
            }
            mBackgroundTimer = new Timer();
            mBackgroundTimer.schedule(new UpdateBackgroundTask(url), BACKGROUND_UPDATE_DELAY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + requestCode + "  " + resultCode);

        if (requestCode == REQUEST_GAME_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                Game game = (Game)data.getSerializableExtra("game");

                if (!(game.isStar() == mSelectGame.isStar()) && mGameSystem == null) {
                    refreshGameList(true);
                } else {
                    int index = mRowsAdapter.indexOf(mSelectGame);
                    DownloadInfo downloadInfo = mSelectGame.getDownloadInfo();
                    if (downloadInfo != null
                            && downloadInfo.getStatus() != DownloadInfo.STATUS_COMPLETED) {
                        game.setDownloadInfo(mSelectGame.getDownloadInfo());
                    }

                    mRowsAdapter.replace(index, game);
                }

                Log.d(TAG, "onActivityResult1");
            } else if (resultCode == Activity.RESULT_FIRST_USER) {
                refreshGameList(false);
            }
        } else if (requestCode == REQUEST_SCRAPER) {
            refreshGameList(false);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void refreshGameList(final boolean refresh) {
        showProgressBar();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createRows(refresh);
            }
        }, 100);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isScanning) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_START:
            case KeyEvent.KEYCODE_MENU:
                showPopupMenu();
                return true;
//            case KeyEvent.KEYCODE_DPAD_CENTER:
//                if (event.isLongPress()) {
//                    showPopupMenu();
//                }
//                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                prePage();
                break;
            case KeyEvent.KEYCODE_BUTTON_R1:
                nextPage();
                break;
        }
        return false;
    }

    public void showScrapOptions(final boolean selectAll) {
        Disposable disposable = mSystemsRepository.getGameSystems(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GameSystem>>() {
                    @Override
                    public void accept(List<GameSystem> gameSystems) throws Exception {
                        showScrapOptions(gameSystems, selectAll);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
    }

    private void showScrapOptions(final List<GameSystem> gameSystems, boolean selectAll) {
        final int size = gameSystems.size();
        String[] systemsTmp = new String[size];
        boolean[] checkedItemsTmp = new boolean[size];

        int j = 0;
        for (int i = 0; i < size; ++i) {
            GameSystem gameSystem = gameSystems.get(i);

            if (gameSystem.isEnable()) {
                systemsTmp[j] = gameSystem.getName();
                checkedItemsTmp[j] = selectAll || mGameSystem != null &&
                        mGameSystem.getName().equals(gameSystem.getName());

                ++j;
            }
        }

        // Remove null value
        final String[] systems = Arrays.copyOf(systemsTmp, j);
        final boolean[] checkedItems = Arrays.copyOf(checkedItemsTmp, j);

        PromptDialog dialog = new PromptDialog()
                .setTitle(R.string.select_scrap_system)
                .setPositiveButton(R.string.ok, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        StringBuilder builder = new StringBuilder();
                        int j = 0;
                        for (int i = 0; i < systems.length; ++i) {
                            if (checkedItems[i]) {
                                builder.append(systems[i]);
                                builder.append(",");
                            }
                        }

                        if (builder.length() > 0) {
                            scraperGames(builder.toString().split(","));
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {

                    }
                })
                .setMultiChoiceItems(systems,
                        checkedItems,
                        new PromptDialog.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which, boolean isChecked) {
                        Log.d(TAG, which + " " + isChecked);
                        checkedItems[which] = isChecked;
                    }
                });
        dialog.show(getFragmentManager(), "SelectScraper");
    }

    private void scraperGames(String[] systems) {
        Intent intent = new Intent(getActivity(), ScraperActivity.class);
        intent.putExtra(ScraperActivity.ARG_GAME_SYSTEMS, systems);
        startActivityForResult(intent, REQUEST_SCRAPER);
    }

    private void showPopupMenu() {
        if (mGameSystem == null) {
            showMenuDialog(R.array.main_star_context_menu);
        } else {
            showMenuDialog(R.array.main_context_menu);
        }
    }

    private void scanCompleted() {
        isScanning = false;
        isScanningOrRefresh = false;
    }

    @Override
    public void progress(DownloadInfo info) {
        if (info.getType() != DownloadInfo.TYPE_ROM) {
            return;
        }

        int i = mRowsAdapter.indexOf(info.getRef());
        if (i != -1) {
            Game currentObj = (Game) mRowsAdapter.get(i);
            Game game = (Game) info.getRef();

            currentObj.setId(game.getId());
            currentObj.setDownloadInfo(info);
            try {
                mRowsAdapter.notifyArrayItemRangeChanged(i, 1);
            } catch (Exception ignore) {}
            isScanningOrRefresh = true;
        }
    }

    @Override
    public void completed(DownloadInfo info) {
        if (info.getType() == DownloadInfo.TYPE_ROM
                && mRowsAdapter.indexOf(info.getRef()) != -1) {
            isScanningOrRefresh = false;
        }
    }

    @Override
    public void onClick(MenuDialog.MenuItem menuItem) {
        switch (menuItem.getId()) {
            case R.string.menu_item_view_info:
                showGameDetail(mAnimateView, mSelectGame, mGameSystem);
                break;
            case R.string.menu_item_pause:
                runOnNewThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                mDownloadService.pause(mSelectGame.getDownloadId());
                            }
                        });
                break;
            case R.string.menu_item_cancel:
                runOnNewThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                mDownloadService.stop(mSelectGame.getDownloadId());
                            }
                        });
                break;
            case R.string.menu_item_restart:
                runOnNewThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mDownloadService.download(mSelectGame.getDownloadInfo());
                                } catch (UrlInvalidException e) {
                                    Utils.showToast(getActivity(), R.string.url_is_invalid);
                                }
                            }
                        });
                break;
            case R.string.menu_item_delete:
                runOnNewThread(new Runnable() {
                    @Override
                    public void run() {
                        mDownloadService.stop(mSelectGame.getDownloadId());
                        Disposable disposable = mGameRepository.deleteGame(mSelectGame)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) throws Exception {
                                        if (aBoolean) {
                                            refreshGameList(false);
                                        }
                                    }
                                }, new Consumer<Throwable>() {
                                    @Override
                                    public void accept(Throwable throwable) throws Exception {
                                        throwable.printStackTrace();
                                    }
                                });
                    }
                });
                break;
            case R.string.get_more_game:
                Intent intent = new Intent(getActivity(), RepoActivity.class);
                intent.putExtra(RepoActivity.ARG_GAME_SYSTEM, mGameSystem);
                startActivity(intent);
                isScanningOrRefresh = true;
                break;
            case R.string.menu_item_refresh:
                refreshGameList(true);
                break;
            case R.string.scrape_games:
                showScrapOptions(false);
                break;
            case R.string.system_info:
                SystemInfoDialog systemInfoDialog = SystemInfoDialog.newInstance(mGameSystem);
                systemInfoDialog.show(getFragmentManager(), "SystemInfoDialog");
                break;
        }
    }

    private void runOnNewThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener  {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            mSelectGame = (Game) item;
            DownloadInfo info = mSelectGame.getDownloadInfo();
            mAnimateView = itemViewHolder.view;

            if (info == null) {
                if (mSelectGame.getStatus() == Game.STATUS_RECOMMENDED) {
                    // Do this to prevent mSelectGame changed before downloadId return
                    final Game game = mSelectGame;
                    final RomActionNoticeDialog dialog = RomActionNoticeDialog
                            .newInstance(game, getString(R.string.install_rom_notice));
                    dialog.setListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == R.id.button_ok) {
                                runOnNewThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DownloadInfo downloadInfo = new DownloadInfo(
                                                game.getName(),
                                                game.getUrl(),
                                                DownloadInfo.TYPE_ROM,
                                                game.getPath(),
                                                game.getVersion());
                                        downloadInfo.setRef(game);
                                        try {
                                            int id = mDownloadService.download(downloadInfo);
                                            game.setDownloadInfo(downloadInfo);
                                            game.setDownloadId(id);
                                            mGameRepository.updateGame(game)
                                                    .subscribeOn(Schedulers.io())
                                                    .subscribe();

                                            Utils.downloadGameDependencies(
                                                    game.getDependencies(),
                                                    mDownloadService,
                                                    game.getPath());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } catch (UrlInvalidException e) {
                                            Utils.showToast(getActivity(), R.string.url_is_invalid);
                                        }
                                    }
                                });
                            }
                            dialog.dismiss();
                        }
                    });
                    dialog.show(getFragmentManager(), "recommendedDownload");
                } else {
                    showGameDetail(
                            mAnimateView,
                            mSelectGame,
                            mGameSystem
                    );
                }
            } else {
                int menus;

                switch (info.getStatus()) {
                    case DownloadInfo.STATUS_COMPLETED:
                        showGameDetail(
                                itemViewHolder.view,
                                mSelectGame,
                                mGameSystem
                        );
                        return;
                    case DownloadInfo.STATUS_DOWNLOADING:
                        menus = R.array.downloading_status_menu;
                        break;
                    case DownloadInfo.STATUS_ERROR:
                        menus = R.array.error_status_menu;
                        break;
                    case DownloadInfo.STATUS_PAUSE:
                        menus = R.array.error_status_menu;
                        break;
                    case DownloadInfo.STATUS_PENDING:
                        menus = R.array.downloading_status_menu;
                        break;
                    case DownloadInfo.STATUS_STOP:
                        menus = R.array.error_status_menu;
                        break;
                    case DownloadInfo.STATUS_AUTO_START:
                    default:
                        return;
                }

                showMenuDialog(menus);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Game) {
                String url = ((Game) item).getBackgroundImageUrl();
                startBackgroundTimer(url);
            }

        }
    }

    private class UpdateBackgroundTask extends TimerTask {
        private String mUrl;

        UpdateBackgroundTask(String url) {
            mUrl = url;
        }

        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateBackground(mUrl);
                }
            });
            mBackgroundTimer.cancel();
        }
    }

    private class DownloadServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadService = ((DownloadService.DownloadBinder) service).getService();
            mDownloadService.registerListener(GameListFragment.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadService = null;
        }
    }
}
