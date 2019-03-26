package com.firefly.emulationstation.commom;

import android.os.Handler;
import android.os.HandlerThread;

import javax.inject.Inject;

/**
 * Created by rany on 18-4-28.
 */

public class IntervalTimer {
    private long mTime = 1000;
    private HandlerThread mThread;
    private Handler mHandler;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mStop) {
                mHandler.postDelayed(mRunnable, mTime);
            }

            mTask.run();
        }
    };
    private Runnable mTask;
    private boolean mStop = false;
    private boolean isStart = false;

    @Inject
    public IntervalTimer() {

    }

    public IntervalTimer(Runnable task, long time) {
        mTask = task;
        mTime = time;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public void setTask(Runnable task) {
        mTask = task;
    }

    public void setHandler(Handler handler) {
        if (!isStart) {
            mHandler = handler;
            mThread = null;
        }
    }

    public void start() {
        if (mHandler == null) {
            mThread = new HandlerThread("IntervalThread");
            mThread.start();

            mHandler = new Handler(mThread.getLooper());
        }

        isStart = true;
        mHandler.postDelayed(mRunnable, mTime);
    }

    public void stop() {
        if (mThread != null) {
            mThread.quitSafely();
        }

        mStop = true;
        isStart = false;
    }
}
