package com.firefly.emulationstation.gamerepo.repogames;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.fragment.MenuDialog;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.commom.presenter.CardPresenter;
import com.firefly.emulationstation.commom.presenter.GameGridPresenter;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * A simple {@link Fragment} subclass.
 */
public class RepoGamesFragment extends Fragment implements RepoGamesContract.View {
    private static final int COL_NUM = 4;

    private GameGridPresenter mGridPresenter;
    private ArrayObjectAdapter mRowsAdapter;
    private VerticalGridPresenter.ViewHolder mGridViewHolder;

    @Inject
    RepoGamesContract.Presenter mPresenter;
    @Inject
    GameRepository mGameRepository;

    private TextView mEmptyView;
    private ProgressBar mProgressBar;

    public RepoGamesFragment() {
        mGridPresenter = new GameGridPresenter();
        mGridPresenter.setNumberOfColumns(COL_NUM);
        mGridPresenter.setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder,
                                      Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                mPresenter.onGameSelected((Game) item, false);
            }
        });

        CardPresenter cardPresenter = new CardPresenter();
        mRowsAdapter = new ArrayObjectAdapter(cardPresenter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_repo_games, container, false);

        mEmptyView = view.findViewById(R.id.empty_view);
        mProgressBar = view.findViewById(R.id.progress_bar);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGridViewHolder = mGridPresenter.onCreateViewHolder((ViewGroup) view);
        VerticalGridView verticalGridView = (VerticalGridView) mGridViewHolder.view;

        ((ViewGroup) view).addView(verticalGridView);
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.subscribe(this);
        mGridPresenter.onBindViewHolder(mGridViewHolder, mRowsAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.unsubscribe();
    }

    @Override
    public void setPresenter(RepoGamesContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showGames(List<Game> games) {
        mRowsAdapter.clear();
        mRowsAdapter.addAll(0, games);

        mGridViewHolder.getGridView().setSelectedPosition(0);
    }

    @Override
    public void setLoading(boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.VISIBLE);
            showEmptyView(false);
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.GONE);
                }
            }, 500);
        }
    }

    @Override
    public void showError(String msg) {
        Utils.showToast(getActivity(), msg);
    }

    @Override
    public void updateDownloadProgress(DownloadInfo downloadInfo) {
        int i = mRowsAdapter.indexOf(downloadInfo.getRef());
        if (i != -1) {
            Game game = (Game) downloadInfo.getRef();
            Game currentObj = (Game) mRowsAdapter.get(i);
            currentObj.setId(game.getId());
            currentObj.setDownloadInfo(downloadInfo);

            try {
                mRowsAdapter.notifyArrayItemRangeChanged(i, 1);
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    public void showInstallNoticeView(final Game game) {
        final RomActionNoticeDialog dialog =
                RomActionNoticeDialog.newInstance(game, getString(R.string.install_rom_notice));
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button_ok) {
                    mPresenter.onGameSelected(game, true);
                }
                dialog.dismiss();
            }
        });
        dialog.show(getFragmentManager(), "install");
    }

    @Override
    public void showRemoveNoticeView(final Game game) {
        final RomActionNoticeDialog dialog =
                RomActionNoticeDialog.newInstance(game, getString(R.string.remove_rom_notice));
        dialog.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button_ok) {
                    mPresenter.onGameSelected(game, true);
                }
                dialog.dismiss();
            }
        });
        dialog.setPositiveButtonText(R.string.uninstall);
        dialog.show(getFragmentManager(), "remove");
    }

    @Override
    public void showGameExistsView(final Game game) {
        PromptDialog dialog = PromptDialog.newInstance(getString(R.string.game_exists));
        PromptDialog.OnClickListener listener = new PromptDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, int which) {
                switch (which) {
                    case Dialog.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                    case Dialog.BUTTON_POSITIVE:
                        mPresenter.downloadGame(game, true);
                        break;
                }
            }
        };

        dialog.setNegativeButton(android.R.string.no, listener);
        dialog.setPositiveButton(android.R.string.yes, listener);
        dialog.show(getFragmentManager(), "prompt");
    }

    @Override
    public void showDownloadManagerMenu(List<MenuDialog.MenuItem> menuItems) {
        MenuDialog dialog = new MenuDialog();
        dialog.setMenuItem(menuItems);
        dialog.setListener(new MenuDialog.OnMenuItemClickListener() {
            @Override
            public void onClick(MenuDialog.MenuItem menuItem) {
                mPresenter.onMenuSelected(menuItem);
            }
        });
        dialog.show(getFragmentManager(), "menu");
    }

    @Override
    public void showEmptyView(boolean show) {
        if (show) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                        mEmptyView.setVisibility(View.VISIBLE);

                }
            }, 500);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }
}
