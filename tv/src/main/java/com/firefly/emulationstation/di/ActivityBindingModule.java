package com.firefly.emulationstation.di;

import com.firefly.emulationstation.MainActivity;
import com.firefly.emulationstation.SplashActivity;
import com.firefly.emulationstation.gamedetail.DetailsActivity;
import com.firefly.emulationstation.gamerepo.GameRepoModule;
import com.firefly.emulationstation.gamerepo.RepoActivity;
import com.firefly.emulationstation.guide.GuideActivity;
import com.firefly.emulationstation.scraper.ScraperActivity;
import com.firefly.emulationstation.search.SearchActivity;
import com.firefly.emulationstation.settings.about.AboutActivity;
import com.firefly.emulationstation.update.NewVersionDialog;
import com.firefly.emulationstation.settings.MainSettingsActivity;
import com.firefly.emulationstation.settings.retroarch.RetroArchInfoActivity;
import com.firefly.emulationstation.update.UpdateDownloadDialog;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by rany on 17-10-25.
 */

@Module
public abstract class ActivityBindingModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();
    @ContributesAndroidInjector
    abstract DetailsActivity contributeDetailsActivity();
    @ContributesAndroidInjector
    abstract SplashActivity contributeSplashActivity();
    @ContributesAndroidInjector
    abstract ScraperActivity contributeScraperActivity();
    @ContributesAndroidInjector
    abstract SearchActivity contributeSearchActivity();
    @ContributesAndroidInjector
    abstract MainSettingsActivity contributeMainSettingsActivity();
    @ContributesAndroidInjector
    abstract GuideActivity contributeGuideActivity();
    @ContributesAndroidInjector
    abstract RetroArchInfoActivity contributeRetroArchInfoActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = {GameRepoModule.class})
    abstract RepoActivity contributeRepoActivity();

    @ContributesAndroidInjector
    abstract NewVersionDialog contributeNewRetroArchDialog();
    @ContributesAndroidInjector
    abstract UpdateDownloadDialog contributeUpdateDownloadDialog();
    @ContributesAndroidInjector
    abstract AboutActivity contributeAboutActivity();
}
