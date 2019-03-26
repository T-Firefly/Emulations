package com.firefly.emulationstation.gamerepo;

import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.BaseDialogActivity;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.gamerepo.category.CategoryContract;
import com.firefly.emulationstation.gamerepo.category.CategoryFragment;
import com.firefly.emulationstation.gamerepo.category.CategoryPresenter;
import com.firefly.emulationstation.gamerepo.data.bean.Category;
import com.firefly.emulationstation.gamerepo.repogames.RepoGamesFragment;
import com.firefly.emulationstation.gamerepo.repogames.RepoGamesPresenter;
import com.firefly.emulationstation.gamerepo.repos.ReposFragment;

import javax.inject.Inject;

public class RepoActivity extends BaseDialogActivity
        implements CategoryContract.Presenter.OnListFragmentInteractionListener {
    public static final String ARG_GAME_SYSTEM = "GameSystem";

    private CategoryFragment mCategoryFragment;
    private RepoGamesFragment mRepoGamesFragment;
    private ReposFragment mReposFragment;

    private Fragment mCurrentFragment;

    @Inject
    RepoGamesPresenter mRepoGamesPresenter;
    @Inject
    CategoryPresenter mCategoryPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_repo);

        GameSystem gameSystem = (GameSystem) getIntent().getSerializableExtra(ARG_GAME_SYSTEM);

        mCategoryFragment = (CategoryFragment) getFragmentManager()
                .findFragmentById(R.id.category);
        mCategoryPresenter.setOnListFragmentInteractionListener(this);
        mCategoryPresenter.setSelectedSystem(gameSystem);

        initFragment();

        setTitle(R.string.game_install);
    }

    @Override
    public void onListFragmentInteraction(Category item) {
        if (item.gameSystem != null) {
            if (mRepoGamesFragment == null) {
                mRepoGamesFragment = new RepoGamesFragment();
                mRepoGamesPresenter.subscribe(mRepoGamesFragment);
            }

            mCurrentFragment = mRepoGamesFragment;
            mRepoGamesPresenter.onCategorySelectChanged(item);
        } else {
            if (mReposFragment == null) {
                mReposFragment = new ReposFragment();

            }

            mCurrentFragment = mReposFragment;
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, mCurrentFragment)
                .commit();
    }

    @Override
    public void onInstallStatusChange(int installStatus) {
        mRepoGamesPresenter.setInstallStatus(installStatus);
    }

    private void initFragment() {
        mRepoGamesFragment = new RepoGamesFragment();
        mRepoGamesPresenter.subscribe(mRepoGamesFragment);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.content, mRepoGamesFragment)
                .commit();

        mCurrentFragment = mRepoGamesFragment;
    }
}
