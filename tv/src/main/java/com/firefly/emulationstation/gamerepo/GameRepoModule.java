package com.firefly.emulationstation.gamerepo;

import com.firefly.emulationstation.di.ActivityScoped;
import com.firefly.emulationstation.di.FragmentScoped;
import com.firefly.emulationstation.gamerepo.category.CategoryContract;
import com.firefly.emulationstation.gamerepo.category.CategoryFragment;
import com.firefly.emulationstation.gamerepo.category.CategoryPresenter;
import com.firefly.emulationstation.gamerepo.repogames.RepoGamesContract;
import com.firefly.emulationstation.gamerepo.repogames.RepoGamesFragment;
import com.firefly.emulationstation.gamerepo.repogames.RepoGamesPresenter;
import com.firefly.emulationstation.gamerepo.repos.ReposContract;
import com.firefly.emulationstation.gamerepo.repos.ReposFragment;
import com.firefly.emulationstation.gamerepo.repos.ReposPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by rany on 18-4-23.
 */

@Module
@ActivityScoped
public abstract class GameRepoModule {
    @Binds
    @ActivityScoped
    abstract CategoryContract.Presenter categoryPresenter(CategoryPresenter presenter);
    @Binds
    @ActivityScoped
    abstract RepoGamesContract.Presenter repoGamesPresenter(RepoGamesPresenter presenter);
    @Binds
    @ActivityScoped
    abstract ReposContract.Presenter reposPresenter(ReposPresenter presenter);

    @ContributesAndroidInjector
    @FragmentScoped
    abstract CategoryFragment contributeCategoryFragment();
    @ContributesAndroidInjector
    @FragmentScoped
    abstract RepoGamesFragment contributeRepoGamesFragment();
    @ContributesAndroidInjector
    @FragmentScoped
    abstract ReposFragment contributeReposFragment();
}
