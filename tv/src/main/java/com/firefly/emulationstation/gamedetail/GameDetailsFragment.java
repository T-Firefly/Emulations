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

package com.firefly.emulationstation.gamedetail;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.DetailsFragment;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.DetailsOverviewRow;
import android.support.v17.leanback.widget.DetailsOverviewRowPresenter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firefly.emulationstation.MainActivity;
import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.GlideApp;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.bean.StarGame;
import com.firefly.emulationstation.data.exceptions.RetroArchCoreNotExistsException;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.gamerepo.repogames.RomActionNoticeDialog;
import com.firefly.emulationstation.scraper.ScraperActivity;
import com.firefly.emulationstation.settings.retroarch.RetroArchInfoActivity;
import com.firefly.emulationstation.utils.Utils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/*
 * LeanbackDetailsFragment extends DetailsFragment, a Wrapper fragment for leanback details screens.
 * It shows a detailed view of video and its meta plus related videos.
 */
public class GameDetailsFragment extends DetailsFragment {
    private static final String TAG = "GameDetailsFragment";

    private static final int ACTION_PALY = 1;
    private static final int ACTION_UPDATE = 2;
    private static final int ACTION_STAR = 3;
    private static final int ACTION_DELETE = 4;
    private static final int ACTION_NOT_AVAILABLE = 5;

    private static final int DETAIL_THUMB_WIDTH = 274;
    private static final int DETAIL_THUMB_HEIGHT = 274;

    private Game mSelectedGame;
    private GameSystem mGameSystem;

    private ArrayObjectAdapter mAdapter;
    private ClassPresenterSelector mPresenterSelector;
    private DetailsOverviewRow mDetailsOverviewRow;

    private BackgroundManager mBackgroundManager;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Inject
    GameRepository mGameRepository;
    @Inject
    SystemsRepository mSystemsRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate DetailsFragment");
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        prepareBackgroundManager();

        Intent args = getActivity().getIntent();
        mSelectedGame = (Game) args.getSerializableExtra(DetailsActivity.GAME);
        mGameSystem = (GameSystem)args.getSerializableExtra(DetailsActivity.SYSTEM);

        if (mGameSystem == null && mSelectedGame instanceof StarGame) {
            mGameSystem = mSystemsRepository.getGameSystem(((StarGame) mSelectedGame).getSystem());
        }

        if (mSelectedGame != null) {
            setupAdapter();
            setupDetailsOverviewRowPresenter();
            setOnItemViewClickedListener(new ItemViewClickedListener());
        } else {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getGameDetail();

        if (mSelectedGame != null) {
            updateBackground(mSelectedGame.getBackgroundImageUrl());
        } else {
            updateBackground(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mDisposables.clear();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mDefaultBackground = getResources().getDrawable(R.color.default_browse_fragment_bg);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    protected void updateBackground(String uri) {

        GlideApp.with(getActivity())
                .load(uri)
                .error(mDefaultBackground)
                .into(new SimpleTarget<Drawable>(mMetrics.widthPixels, mMetrics.heightPixels) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition transition) {
                        mBackgroundManager.setDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);

                        mBackgroundManager.setDrawable(errorDrawable);
                    }
                });
    }

    private void setupAdapter() {
        mPresenterSelector = new ClassPresenterSelector();
        mAdapter = new ArrayObjectAdapter(mPresenterSelector);
        setAdapter(mAdapter);
    }

    private void setupDetailsOverviewRow() {
        mDetailsOverviewRow = new DetailsOverviewRow(mSelectedGame);
        File file = new File(mSelectedGame.getPath());
        mDetailsOverviewRow.setImageDrawable(getResources().getDrawable(R.drawable.detail_card_img));

        updateGameBoxArt();

        if (file.exists()) {
            mDetailsOverviewRow.addAction(new Action(ACTION_PALY, getResources().getString(
                    R.string.play_trailer_1), null));
        } else {
            mDetailsOverviewRow.addAction(new Action(ACTION_NOT_AVAILABLE, getResources().getString(
                    R.string.play_trailer_2), null));
        }

        mDetailsOverviewRow.addAction(new Action(ACTION_UPDATE, getResources().getString(R.string.update_1),
                null));
        if (mGameSystem != null) {
            mDetailsOverviewRow.addAction(new Action(ACTION_STAR, getResources().getString(
                    mSelectedGame.isStar() ? R.string.unstar : R.string.star), null));
        }

        if (!file.exists() || file.canWrite()) {
            mDetailsOverviewRow.addAction(new Action(ACTION_DELETE, getResources().getString(
                    R.string.uninstall), null));
        }

        mAdapter.clear();
        mAdapter.add(mDetailsOverviewRow);
    }

    private void updateGameBoxArt() {
        Activity activity = getActivity();

        if (activity == null) {
            return;
        } else if (mSelectedGame.getCardImageUrl() == null) {
            mDetailsOverviewRow.setImageDrawable(activity.getDrawable(R.drawable.detail_card_img));
            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
            return;
        }

        int width = Utils.convertDpToPixel(activity, DETAIL_THUMB_WIDTH);
        int height = Utils.convertDpToPixel(activity, DETAIL_THUMB_HEIGHT);

        GlideApp.with(activity)
                .load(mSelectedGame.getCardImageUrl())
                .fitCenter()
                .placeholder(R.drawable.detail_card_img)
                .error(R.drawable.detail_card_img)
                .into(new SimpleTarget<Drawable>(width, height) {
                    @Override
                    public void onResourceReady(@Nullable Drawable resource,
                                                @Nullable Transition transition) {
                        Log.d(TAG, "image url ready: " + resource);
                        mDetailsOverviewRow.setImageDrawable(resource);
                        mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                    }
                });
    }

    private void setupDetailsOverviewRowPresenter() {
        // Set detail background and style.
        DetailsOverviewRowPresenter detailsPresenter =
                new DetailsOverviewRowPresenter(new DetailsDescriptionPresenter());
        detailsPresenter.setBackgroundColor(getResources().getColor(R.color.detail_row_background));
        detailsPresenter.setStyleLarge(true);

        // Hook up transition element.
        detailsPresenter.setSharedElementEnterTransition(getActivity(),
                DetailsActivity.SHARED_ELEMENT_NAME);

        detailsPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == ACTION_PALY) {
                    File file = new File(mSelectedGame.getPath());
                    if (file.exists()) {
                        try {
                            tryToStartGame();
                        } catch (ActivityNotFoundException | RetroArchCoreNotExistsException e) {
                            showStarGameExceptionDialog(e);
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.game_not_exists, Toast.LENGTH_SHORT).show();
                    }
                } else if (action.getId() == ACTION_UPDATE) {
                    Intent intent = new Intent(getActivity(), ScraperActivity.class);
                    intent.putExtra(ScraperActivity.ARG_GAMES, new long[]{mSelectedGame.getId()});
                    intent.putExtra(ScraperActivity.ARG_AUTO_SELECT, false);
                    startActivity(intent);
                } else if (action.getId() == ACTION_DELETE) {
                    showDeleteNoticeDialog();
                } else if (action.getId() == ACTION_STAR) {
                    if (mSelectedGame.isStar()) {
                        starGame(action, false);
                    } else {
                        starGame(action, true);
                    }
                }else if (action.getId() == ACTION_NOT_AVAILABLE){
                    Toast.makeText(getActivity(), R.string.game_not_exists, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), action.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mPresenterSelector.addClassPresenter(DetailsOverviewRow.class, detailsPresenter);
    }

    private void tryToStartGame() throws ActivityNotFoundException, RetroArchCoreNotExistsException {
        if (mGameSystem != null) {
            Utils.startGame(getActivity(), mGameSystem, mSelectedGame);
        } else {
            final List<GameSystem> systems = mSystemsRepository
                    .getMatchGameSystems(mSelectedGame.getRomPathId(), mSelectedGame.getExt());

            if (systems.size() == 1) {
                Utils.startGame(getActivity(), systems.get(0), mSelectedGame);
                return;
            } else if (systems.isEmpty()) {
                Utils.showToast(getActivity(), R.string.no_system_to_start);
                return;
            }

            String[] systemNames = new String[systems.size()];
            int i = 0;
            for (GameSystem system : systems) {
                systemNames[i++] = system.getName();
            }

            new PromptDialog()
                    .setTitle(R.string.select_system_to_start)
                    .setSingleChoiceItems(systemNames, 0,
                            new PromptDialog.OnClickListener() {
                        @Override
                        public void onClick(DialogFragment dialog, int which) {
                            GameSystem system = systems.get(which);
                            try {
                                Utils.startGame(getActivity(), system, mSelectedGame);
                            } catch (RetroArchCoreNotExistsException e) {
                                showStarGameExceptionDialog(e);
                            }
                            dialog.dismiss();
                        }
                    })
                    .show(getFragmentManager(), "SelectSystem");
        }
    }

    private void showStarGameExceptionDialog(Throwable e) {
        String title;
        int nButton;
        int pButton;

        if (e instanceof ActivityNotFoundException) {
            title = getString(R.string.retroarch_not_install);
            nButton = android.R.string.no;
            pButton = android.R.string.yes;
        } else if (e instanceof RetroArchCoreNotExistsException) {
            String path = ((RetroArchCoreNotExistsException) e).getRef().toString();
            String core = path.substring(path.lastIndexOf('/')+1);

            title = getString(R.string.retroarch_core_not_exists, core);
            nButton = android.R.string.cancel;
            pButton = R.string.try_to_download;
        } else {
            e.printStackTrace();
            return;
        }

        PromptDialog dialog = PromptDialog.newInstance(title);
        PromptDialog.OnClickListener listener = new PromptDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, int which) {
                if (which == Dialog.BUTTON_POSITIVE) {
                    Intent intent = new Intent(getActivity(), RetroArchInfoActivity.class);
                    startActivity(intent);
                }

                dialog.dismiss();
            }
        };

        dialog.setNegativeButton(nButton, listener);
        dialog.setPositiveButton(pButton, listener);
        dialog.show(getFragmentManager(), "no retroarch");
    }

    private void starGame(final Action action, boolean star) {
        Disposable disposable = mGameRepository.star(mSelectedGame, mGameSystem, star)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long status) throws Exception {
                        if (status <= 0) {
                            Utils.showToast(getActivity(), R.string.star_failed);
                        } else {
                            action.setLabel1(
                                    getString(mSelectedGame.isStar() ?
                                            R.string.unstar :
                                            R.string.star));
                            mDetailsOverviewRow.getActionsAdapter()
                                    .notifyItemRangeChanged(0,
                                            mDetailsOverviewRow.getActions().size());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private void showDeleteNoticeDialog() {
        final RomActionNoticeDialog dialog = RomActionNoticeDialog
                .newInstance(mSelectedGame, getString(R.string.remove_rom_notice));
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button_ok) {
                    processDelete();
                }
                dialog.dismiss();
            }
        });
        dialog.setPositiveButtonText(R.string.uninstall);
        dialog.show(getFragmentManager(), "remove");
    }

    private void processDelete() {
        Disposable disposable = mGameRepository.deleteGame(mSelectedGame)
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            if (aBoolean) {
                                Utils.showToast(getActivity(), R.string.deleted);
                                getActivity().setResult(Activity.RESULT_FIRST_USER);
                                getActivity().finish();
                            } else {
                                Utils.showToast(getActivity(), R.string.delete_failed);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
    }

    private void setupGameListRow() {
//        String subcategories[] = {getString(R.string.related_movies)};
//        List<Game> list = null;
//
//        Collections.shuffle(list);
//        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
//        for (int j = 0; j < NUM_COLS; j++) {
//            listRowAdapter.add(list.get(j % 5));
//        }

//        HeaderItem header = new HeaderItem(0, subcategories[0]);
//        mAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void setupMovieListRowPresenter() {
        mPresenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Game) {
                Game game = (Game) item;
                Log.d(TAG, "Item: " + item.toString());
                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(getResources().getString(R.string.game), mSelectedGame);
                intent.putExtra(getResources().getString(R.string.should_start), true);
                startActivity(intent);


                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        ((ImageCardView) itemViewHolder.view).getMainImageView(),
                        DetailsActivity.SHARED_ELEMENT_NAME).toBundle();
                getActivity().startActivity(intent, bundle);
            }
        }
    }

    private void getGameDetail() {
        Disposable disposable = mGameRepository
                .getGameDetail(mSelectedGame.getId(),
                        GameSystem.getPlatformName(mSelectedGame.getPlatformId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Game>() {
                    @Override
                    public void accept(Game game) throws Exception {
                        // Only merge when mSelectedGame is not scraped,
                        // and update mSelectedGame for some case e.g download completed moment
                        if (mSelectedGame != null
                                && !mSelectedGame.isScraped()) {
                            mSelectedGame.merge(game);
                        } else {
                            mSelectedGame = game;
                        }
                        setResult();

                        if (mDetailsOverviewRow == null) {
                            setupDetailsOverviewRow();
                            setupGameListRow();
                            setupMovieListRowPresenter();
                        } else {
                            mAdapter.notifyArrayItemRangeChanged(0, mAdapter.size());
                            updateGameBoxArt();
                        }

                        updateBackground(mSelectedGame.getBackgroundImageUrl());

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });

        mDisposables.add(disposable);
    }

    private void setResult() {
        Intent intent = getActivity().getIntent();
        intent.putExtra("game", mSelectedGame);
        getActivity().setResult(Activity.RESULT_OK, intent);
    }
}
