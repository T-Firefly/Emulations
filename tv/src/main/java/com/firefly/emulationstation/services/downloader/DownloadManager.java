package com.firefly.emulationstation.services.downloader;

import android.content.SharedPreferences;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.DownloadInfo;

import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by rany on 18-3-26.
 */

@Singleton
public class DownloadManager {
    private ThreadPoolExecutor mRomDownloadPoolExecutor;
    private ThreadPoolExecutor mOtherDownloadPoolExecutor;

    @Inject
    DownloadManager(SharedPreferences settings) {
        int poolSize = settings.getInt(
                Constants.SETTINGS_MAX_DOWNLOAD_THREAD, Constants.DEFAULT_DOWNLOAD_THREAD);
        mRomDownloadPoolExecutor = new DownloadThreadPoolExecutor(poolSize);
        mOtherDownloadPoolExecutor = new DownloadThreadPoolExecutor(5);
    }

    public void addTask(IDownloadTask runnable) {
        DownloadInfo downloadInfo = runnable.getDownloadInfo();
        if (isRom(downloadInfo)) {
            mRomDownloadPoolExecutor.execute(runnable);
        } else {
            mOtherDownloadPoolExecutor.execute(runnable);
        }
    }

    public void setPoolSize(int size) {
        mRomDownloadPoolExecutor.setCorePoolSize(size);
    }

    public void removeTask(IDownloadTask runnable) {
        runnable.cancel();
        DownloadInfo info = runnable.getDownloadInfo();
        if (isRom(info)) {
            mRomDownloadPoolExecutor.remove(runnable);
        } else {
            mOtherDownloadPoolExecutor.remove(runnable);
        }
    }

    private boolean isRom(DownloadInfo info) {
        return info.getType() == DownloadInfo.TYPE_ROM
                || info.getType() == DownloadInfo.TYPE_ROM_DEPENDENCY;
    }
}
