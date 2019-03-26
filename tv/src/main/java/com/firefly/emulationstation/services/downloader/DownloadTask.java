package com.firefly.emulationstation.services.downloader;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.repository.DownloadRepository;

/**
 * Created by rany on 18-3-26.
 */

public class DownloadTask implements IDownloadTask {
    private DownloadInfo downloadInfo;
    private DownloadThread thread;

    private DownloadRepository mDownloadRepository;

    public DownloadTask(DownloadInfo downloadInfo,
                        DownloadRepository downloadRepository) {
        this.downloadInfo = downloadInfo;
        mDownloadRepository = downloadRepository;
    }

    @Override
    public void cancel() {
        if (thread != null) {
            thread.pause();
        }
    }

    @Override
    public void pauseToWaiting() {
        downloadInfo.setStatus(DownloadInfo.STATUS_PENDING);
        mDownloadRepository.save(downloadInfo);
        cancel();
    }

    @Override
    public void run() {
        downloadInfo.setStatus(DownloadInfo.STATUS_DOWNLOADING);
        mDownloadRepository.save(downloadInfo);

        if (thread == null) {
            this.thread = new DownloadThread(downloadInfo);
        }

        thread.run();
    }

    @Override
    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }

    @Override
    public DownloadThread getDownloadThread() {
        return thread;
    }
}
