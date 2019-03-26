package com.firefly.emulationstation.services.downloader;

import com.firefly.emulationstation.data.bean.DownloadInfo;

/**
 * Created by rany on 18-5-8.
 */

public interface IDownloadTask extends Runnable {
    DownloadInfo getDownloadInfo();
    DownloadThread getDownloadThread();
    void cancel();
    void pauseToWaiting();
}
