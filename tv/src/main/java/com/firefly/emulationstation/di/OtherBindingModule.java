package com.firefly.emulationstation.di;

import com.firefly.emulationstation.MediaMountedReceiver;
import com.firefly.emulationstation.services.RetroArchDownloadService;
import com.firefly.emulationstation.services.VersionCheckService;
import com.firefly.emulationstation.services.DownloadReceiver;
import com.firefly.emulationstation.services.downloader.DownloadService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by rany on 18-2-8.
 */

@Module
abstract class OtherBindingModule {
    @ContributesAndroidInjector
    abstract MediaMountedReceiver contributeMediaMountedReceiver();
    @ContributesAndroidInjector
    abstract DownloadService contributeDownloadService();
    @ContributesAndroidInjector
    abstract DownloadReceiver contributeRomDownloadCompletedReceiver();
    @ContributesAndroidInjector
    abstract VersionCheckService contributeRetroArchVersionService();
    @ContributesAndroidInjector
    abstract RetroArchDownloadService contributeRetroArchDownloadService();
}
