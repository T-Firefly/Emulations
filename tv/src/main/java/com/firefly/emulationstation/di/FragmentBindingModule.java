package com.firefly.emulationstation.di;

import com.firefly.emulationstation.MainFragment;
import com.firefly.emulationstation.gamedetail.GameDetailsFragment;
import com.firefly.emulationstation.gamelist.GameListFragment;
import com.firefly.emulationstation.guide.GuideFragment;
import com.firefly.emulationstation.scraper.ScraperOptionsFragment;
import com.firefly.emulationstation.search.SearchFragment;
import com.firefly.emulationstation.settings.about.VersionFragment;
import com.firefly.emulationstation.settings.retroarch.InfoFragment;
import com.firefly.emulationstation.settings.systems.SystemsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by rany on 17-10-26.
 */

@Module
public abstract class FragmentBindingModule {
    @ContributesAndroidInjector
    abstract GameListFragment contributeGameListFragment();
    @ContributesAndroidInjector
    abstract GameDetailsFragment contributeGameDetailFragment();
    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragment();
    @ContributesAndroidInjector
    abstract ScraperOptionsFragment contributeScraperOptionsFragment();
    @ContributesAndroidInjector
    abstract SearchFragment contributeSearchFragment();
    @ContributesAndroidInjector
    abstract SystemsFragment contributeSystemsFragment();
    @ContributesAndroidInjector
    abstract GuideFragment contributeGuideFragment();
    @ContributesAndroidInjector
    abstract InfoFragment contributeInfoFragment();
    @ContributesAndroidInjector
    abstract VersionFragment contributeVersionFragment();
}
