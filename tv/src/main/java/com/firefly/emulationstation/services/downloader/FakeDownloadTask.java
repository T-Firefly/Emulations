package com.firefly.emulationstation.services.downloader;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.repository.DownloadRepository;

/**
 * Created by rany on 18-5-8.
 */

public class FakeDownloadTask extends DownloadTask {
    private DownloadRepository mDownloadRepository;
    private boolean mStop = false;

    FakeDownloadTask(DownloadInfo info,
                     DownloadRepository downloadRepository) {
        super(info, downloadRepository);

        mDownloadRepository = downloadRepository;
        info.setSize(100);
    }

    @Override
    public void run() {
        getDownloadInfo().setStatus(DownloadInfo.STATUS_DOWNLOADING);
        mDownloadRepository.save(getDownloadInfo());

        while (!mStop) {
            long progress = getDownloadInfo().getProgress();
            getDownloadInfo().setProgress((++progress) % 100);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel() {
        mStop = true;
    }
}
