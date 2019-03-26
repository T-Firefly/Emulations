package com.firefly.emulationstation.search;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.ProgressBarManager;
import android.support.v17.leanback.app.SearchSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SearchEditText;
import android.support.v17.leanback.widget.SpeechRecognitionCallback;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.presenter.CardListRowPresenter;
import com.firefly.emulationstation.commom.presenter.CardPresenter;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.gamedetail.DetailsActivity;
import com.firefly.emulationstation.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link SearchSupportFragment} subclass.
 */
public class SearchFragment extends SearchSupportFragment
        implements SearchSupportFragment.SearchResultProvider {

    private static final String TAG = SearchFragment.class.getSimpleName();
    private ViewGroup mViewGroup;
    private SearchEditText mSearchEditText;

    @Inject
    GameRepository mGameRepository;
    @Inject
    ProgressBarManager mProgressBarManager;
    @Inject
    SystemsRepository mSystemsRepository;
    private ArrayObjectAdapter mRowsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);

        ListRowPresenter presenter = new CardListRowPresenter();
        mRowsAdapter = new ArrayObjectAdapter(presenter);

        presenter.setShadowEnabled(false);
        setSearchResultProvider(this);
        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                      RowPresenter.ViewHolder rowViewHolder, Row row) {
                Game game = (Game) item;

                Intent intent = new Intent(getActivity(), DetailsActivity.class);
                intent.putExtra(DetailsActivity.GAME, game);
                startActivity(intent);
            }
        });

        if (!Utils.isAppInstall(getActivity(), "com.google.android.googlequicksearchbox")) {
            setSpeechRecognitionCallback(new SpeechRecognitionCallback() {
                @Override
                public void recognizeSpeech() {
                }
            });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewGroup = (ViewGroup)view;
        mSearchEditText = mViewGroup.findViewById(R.id.lb_search_text_editor);
        mViewGroup.findViewById(R.id.lb_search_bar_speech_orb)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.showToast(getContext(), R.string.not_support);
                    }
                });
    }

    @Override
    public ObjectAdapter getResultsAdapter() {
        return mRowsAdapter;
    }

    @Override
    public boolean onQueryTextChange(String newQuery) {
        if (newQuery.length() >= 3) {
            newQuery = String.format("%%%s%%", newQuery);
            search(newQuery);

            return true;
        }

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        query = String.format("%%%s%%", query);
        search(query);

        return true;
    }

    private void search(final String keyword) {
        showProgress();
        mGameRepository.searchGames(keyword)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<List<Game>, Map<String, List<Game>>>() {
                    @Override
                    public Map<String, List<Game>> apply(List<Game> games) throws Exception {
                        Log.d(TAG, Thread.currentThread().getName());
                        return classify(games);
                    }
                })
                .subscribe(new Consumer<Map<String, List<Game>>>() {
                    @Override
                    public void accept(Map<String, List<Game>> games) throws Exception {
                        mRowsAdapter.clear();

                        if (games.isEmpty()) {
                            ArrayObjectAdapter listRowAdapter =
                                    new ArrayObjectAdapter(new CardPresenter());
                            String header = getString(R.string.no_game_found);

                            HeaderItem headerItem = new HeaderItem(header);
                            mRowsAdapter.add(new ListRow(headerItem, listRowAdapter));

                            listRowAdapter.addAll(0, new ArrayList());
                            mProgressBarManager.hide();
                        } else {
                            showGames(games);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        mProgressBarManager.hide();
                    }
                });
    }

    private void showProgress() {
        mProgressBarManager.setRootView(mViewGroup);
        mProgressBarManager.setInitialDelay(200);
        mProgressBarManager.show();
    }

    private Map<String, List<Game>> classify(List<Game> games) {
        Map<String, List<Game>> categories = new HashMap<>(100);
        Iterator<Game> iterator = games.iterator();

        while (iterator.hasNext()) {
            Game game = iterator.next();
            String header = GameSystem.getPlatformName(game.getPlatformId());

            if (header == null) {
                header = getString(R.string.unknown_platform);
            }

            List<Game> systemGames = categories.get(header);
            if (systemGames == null) {
                systemGames = new ArrayList<>();
                categories.put(header, systemGames);
            }
            systemGames.add(game);
            iterator.remove();
        }

        return categories;
    }

    private void showGames(final Map<String, List<Game>> games) {
        TreeMap<String, List<Game>> sortMap = new TreeMap<>(games);

        for (Map.Entry<String, List<Game>> category : sortMap.entrySet()) {
            ArrayObjectAdapter listRowAdapter =
                    new ArrayObjectAdapter(new CardPresenter());

            HeaderItem headerItem = new HeaderItem(category.getKey());
            mRowsAdapter.add(new ListRow(headerItem, listRowAdapter));

            listRowAdapter.addAll(0, category.getValue());
        }

        mProgressBarManager.hide();

    }
}
