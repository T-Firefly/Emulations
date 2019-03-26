/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.firefly.emulationstation;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlightHelper;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.PageRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.commom.IKeyDownEvent;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.gamedetail.DetailsActivity;
import com.firefly.emulationstation.gamelist.GameHeaderItem;
import com.firefly.emulationstation.gamelist.GameListFragment;
import com.firefly.emulationstation.search.SearchActivity;
import com.firefly.emulationstation.settings.SettingsFragment;
import com.firefly.emulationstation.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainFragment extends BrowseFragment
        implements IKeyDownEvent {
    private static final String TAG = "MainFragment";

    public static final long STAR_HEADER_ID = 0;
    public static final long SETTINGS_HEADER_ID = 1;

    @Inject
    SystemsRepository mSystemsRepository;
    @Inject
    GameRepository mGameRepository;
    @Inject
    SharedPreferences mSettings;

    private ArrayObjectAdapter mRowsAdapter;
    private Fragment mSelectFragment;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    private PageRow mStarPage;
    private boolean mGameSystemChanged = false;
    private BroadcastReceiver mSystemChangedReceiver;
    private List<GameSystem> mGameSystems = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSystemChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mGameSystemChanged = true;
            }
        };
        IntentFilter intentFilter = new IntentFilter(Constants.BROADCAST_GAME_SYSTEM_CHANGED);
        LocalBroadcastManager
                .getInstance(getActivity())
                .registerReceiver(mSystemChangedReceiver, intentFilter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        AndroidInjection.inject(this);
        super.onActivityCreated(savedInstanceState);

        setupUIElements();

        getMainFragmentRegistry().registerFragment(PageRow.class,
                new PageRowFragmentFactory());

        mStarPage = new PageRow(
                new GameHeaderItem(
                        STAR_HEADER_ID,
                        getString(R.string.header_stars)
                )
        );

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        loadRows(true);

        setupEventListeners();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            view.setBackgroundColor(0x60000000);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // in case of user disable or enable systems
        // in Settings Systems.
        if (mGameSystemChanged) {
            mGameSystemChanged = false;
            loadRows(false);
        }

        final int index = mRowsAdapter.indexOf(mStarPage);
        Disposable disposable = mGameRepository.starCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer starSum) throws Exception {
                        if (mSettings.getBoolean(Constants.SETTINGS_SHOW_STAR_GAMES_HEADER, true) &&
                                starSum > 0) {
                            if (index == -1) {
                                mRowsAdapter.add(0, mStarPage);
                            }
                        } else {
                            if (index != -1) {
                                mRowsAdapter.remove(mStarPage);
                                if (getSelectedPosition() == index) {
                                    setSelectedPosition(index);
                                }
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });

    }

    @Override
    public void onPause() {
        super.onPause();

        mDisposables.clear();
    }

    @Override
    public void onStop() {
        super.onStop();

        LocalBroadcastManager
                .getInstance(getActivity())
                .unregisterReceiver(mSystemChangedReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void loadRows(boolean refresh) {
        mRowsAdapter.clear();

        Disposable disposable = mSystemsRepository.getGameSystems(refresh)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GameSystem>>() {
                    @Override
                    public void accept(List<GameSystem> gameSystems) throws Exception {
                        if (gameSystems == null || gameSystems.isEmpty()) {
                            getActivity().finish();
                            return;
                        }
                        mGameSystems = gameSystems;
                        for (GameSystem gs : gameSystems) {
                            if (!gs.isEnable()) {
                                continue;
                            }

                            GameHeaderItem gridHeader = new GameHeaderItem(gs.getName(), gs);
                            mRowsAdapter.add(new PageRow(gridHeader));
                        }

                        fistStartCheck();

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                        Utils.showToast(getActivity(), R.string.systems_file_invalid);
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        mRowsAdapter.add(new PageRow(
                                new GameHeaderItem(
                                        SETTINGS_HEADER_ID,
                                        getString(R.string.settings)
                                )
                        ));
                        startEntranceTransition();
                    }
                });

        mDisposables.add(disposable);
    }

    private void fistStartCheck() {
        if (mSettings.getBoolean(Constants.SETTINGS_FIRST_START, true)) {
            PromptDialog dialog = new PromptDialog()
                    .setMessage(R.string.first_start_check)
                    .setPositiveButton(android.R.string.yes, new PromptDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogFragment dialog, int which) {
                            for (GameSystem gameSystem : mGameSystems) {
                                mGameRepository.getGameList(gameSystem, true)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            clearFirstStart();
                        }
                    });

            dialog.show(getFragmentManager(), "ScanGame");
        }
    }

    private void showScrapeDialog() {
        PromptDialog dialog = new PromptDialog()
                .setMessage(R.string.notice_for_scrape_game)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        if (mSelectFragment instanceof GameListFragment) {
                            GameListFragment fragment = (GameListFragment) mSelectFragment;
                            fragment.showScrapOptions(true);
                        }
                    }
                });

        dialog.show(getFragmentManager(), "Scraper");
    }

    private void setupUIElements() {
        // setBadgeDrawable(getActivity().getResources().getDrawable(
        // R.drawable.videos_by_google_banner));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent
        // over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // set fastLane (or headers) background color
        setBrandColor(getResources().getColor(R.color.fastlane_background));
        // set search icon color
        setSearchAffordanceColor(getResources().getColor(R.color.search_opaque));

        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });

        // Disable header item focus scale
        FocusHighlightHelper.setupHeaderItemFocusHighlight(
                getHeadersFragment().getBridgeAdapter(), false);

        prepareEntranceTransition();
    }

    private void setupEventListeners() {
        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mSelectFragment instanceof IKeyDownEvent && !isShowingHeaders()) {
            return ((IKeyDownEvent)mSelectFragment).onKeyDown(keyCode, event);
        }

        return false;
    }

    private void clearFirstStart() {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(Constants.SETTINGS_FIRST_START, false);
        editor.apply();
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            Game game = (Game) item;
            Log.d(TAG, "Item: " + item.toString());
            Intent intent = new Intent(getActivity(), DetailsActivity.class);
            intent.putExtra(DetailsActivity.GAME, game);

            Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    getActivity(),
                    ((ImageCardView) itemViewHolder.view).getMainImageView(),
                    DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
            getActivity().startActivity(intent, bundle);
        }
    }

    private class PageRowFragmentFactory extends BrowseFragment.FragmentFactory {

        @Override
        public Fragment createFragment(Object rowObj) {
            Row row = (Row)rowObj;
            GameHeaderItem headerItem = (GameHeaderItem)row.getHeaderItem();

            if (headerItem.getGameSystem() != null) {
                GameSystem system = headerItem.getGameSystem();

                mSelectFragment = new GameListFragment();
                Bundle args = new Bundle();

                args.putSerializable("system", system);
                mSelectFragment.setArguments(args);

                setTitle(system.getName());
            } else {
                if (headerItem.getId() == SETTINGS_HEADER_ID) {
                    mSelectFragment = new SettingsFragment();
                    setTitle(getString(R.string.settings));
                } else {
                    mSelectFragment = new GameListFragment();
                    setTitle(getString(R.string.header_stars));
                }
            }

            return mSelectFragment;
        }
    }
}
