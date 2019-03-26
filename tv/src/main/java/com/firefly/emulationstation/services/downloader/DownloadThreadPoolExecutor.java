package com.firefly.emulationstation.services.downloader;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DownloadThreadPoolExecutor extends ThreadPoolExecutor {
    private static final String TAG = DownloadThreadPoolExecutor.class.getSimpleName();

    private LinkedBlockingDeque<IDownloadTask> mDownloadingQueue = new LinkedBlockingDeque<>();
    private LinkedBlockingDeque<Runnable> mWaitingQueue;

    DownloadThreadPoolExecutor(int poolSize) {
        super(poolSize, poolSize, 0,
                TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
        mWaitingQueue = (LinkedBlockingDeque<Runnable>) getQueue();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        mDownloadingQueue.add((IDownloadTask)r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        mDownloadingQueue.remove(r);
    }

    @Override
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < getPoolSize()) {
            super.setCorePoolSize(corePoolSize);
            setMaximumPoolSize(corePoolSize);
        } else {
            setMaximumPoolSize(corePoolSize);
            super.setCorePoolSize(corePoolSize);
        }

        int poolSize = getPoolSize();
        int stopSize = poolSize - corePoolSize;

        if (stopSize <= 0) {
            return;
        }

        ArrayList<IDownloadTask> willStop = new ArrayList<>(stopSize);
        while (stopSize-- > 0) {
            IDownloadTask task = mDownloadingQueue.pollLast();
            if (task == null) {
                break;
            }

            task.pauseToWaiting();
            willStop.add(0, (IDownloadTask) task);
        }

        mWaitingQueue.addAll(willStop);
    }

    public LinkedBlockingDeque<IDownloadTask> getDownloadingQueue() {
        return mDownloadingQueue;
    }
}
