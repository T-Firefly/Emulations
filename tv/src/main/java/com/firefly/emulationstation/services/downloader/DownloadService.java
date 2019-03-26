package com.firefly.emulationstation.services.downloader;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.webkit.URLUtil;

import com.firefly.emulationstation.BuildConfig;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.exceptions.UrlInvalidException;
import com.firefly.emulationstation.data.repository.DownloadRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.commom.Constants.BROADCAST_DOWNLOAD_COMPLETED;
import static com.firefly.emulationstation.commom.Constants.BROADCAST_DOWNLOAD_ERROR;

public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();
    public static final String ACTION_START_NOT_COMPLETE = "action_start_not_complete";
    private static final int BUSY_TIMER = 600;
    private static final int WAITING_TIMER = 1000;

    @Inject
    NotificationHelper mNotificationHelper;
    @Inject
    DownloadRepository mDownloadRepository;
    @Inject
    DownloadManager mDownloadManager;

    private final SparseArray<IDownloadTask> mDownloads = new SparseArray<>();
    private final Set<DownloadListener> mListeners = new HashSet<>();
    private DownloadBinder mBinder = new DownloadBinder();

    private Handler mNotificationHandler;
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            synchronized (mDownloads) {
                boolean hasDownloading = false;
                for(int i = 0, size = mDownloads.size(); i < size; i++) {
                    final IDownloadTask task = mDownloads.valueAt(i);

                    if (task == null) {
                        continue;
                    }

                    final DownloadInfo info = task.getDownloadInfo();

                    mNotificationHelper.showNotification(info);

                    if (info.getStatus() == DownloadInfo.STATUS_DOWNLOADING
                            || info.getStatus() == DownloadInfo.STATUS_PENDING) {
                        hasDownloading = true;
                    } else {
                        task.cancel();
                        mDownloads.remove(info.getId());

                        if (info.getStatus() == DownloadInfo.STATUS_COMPLETED) {
                            Intent completedIntent = new Intent(BROADCAST_DOWNLOAD_COMPLETED);
                            completedIntent.putExtra("info", info);
                            sendBroadcast(completedIntent);
                        } else if (info.getStatus() == DownloadInfo.STATUS_ERROR) {
                            Intent completedIntent = new Intent(BROADCAST_DOWNLOAD_ERROR);
                            completedIntent.putExtra("info", info);
                            sendBroadcast(completedIntent);
                        }
                    }
                    if (BuildConfig.DEBUG)
                        Log.d(TAG, info.getId() + " " + info.getName() + ": " +
                                info.getProgress() + " " + info.getSize() + " " + info.getStatus());

                    invokeListener(info);

                    // update database
                    mDownloadRepository.save(info);
                }

                int time = WAITING_TIMER;
                if (hasDownloading) {
                    time = BUSY_TIMER;
                }

                mNotificationHandler.postDelayed(mRunnable, time);
            }
        }
    };

    public DownloadService() {
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (ACTION_START_NOT_COMPLETE.equals(action)) {
                continueDownload();
            }
        }

        return START_REDELIVER_INTENT;
    }

    private void continueDownload() {
        mDownloadRepository.findWithGameByStatus(
                DownloadInfo.STATUS_PAUSE, DownloadInfo.STATUS_DOWNLOADING, DownloadInfo.STATUS_PENDING)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<DownloadInfo>>() {
                    @Override
                    public void accept(List<DownloadInfo> list) throws Exception {
                        for (DownloadInfo info : list) {
                            try {
                                download(info);
                            } catch (UrlInvalidException e) {
                                Log.d(TAG, info.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        startService(new Intent(this, DownloadService.class));

        return mBinder;
    }

    @Override
    public void onDestroy() {
        pauseAll();

        super.onDestroy();
    }

    public int download(DownloadInfo info) throws UrlInvalidException {
        if (TextUtils.isEmpty(info.getUrl()) || !URLUtil.isNetworkUrl(info.getUrl())) {
            throw new UrlInvalidException();
        }

        IDownloadTask task = mDownloads.get(info.getId());

        if (task != null) {
            return info.getId();
        }

        startDownloadTask(info);

        return info.getId();
    }

    private void saveWithStatus(int id, int status) {
        IDownloadTask task = mDownloads.get(id);

        // remove current download
        if (task != null) {
            task.cancel();
            DownloadInfo info = task.getDownloadInfo();

            if (info != null) {
                info.setStatus(status);
                if (status == DownloadInfo.STATUS_STOP) {
                    info.setProgress(-1);
                }

                mDownloadRepository.save(info);

                mDownloadManager.removeTask(task);
            }
        } else { // Update database
            DownloadInfo info = mDownloadRepository.findOne(id);

            if (info != null) {
                info.setStatus(status);
                if (status == DownloadInfo.STATUS_STOP) {
                    info.setProgress(-1);
                }

                mDownloadRepository.save(info);

                // For update view to new status
                for (DownloadListener listener : mListeners) {
                    listener.progress(info);
                }
            }
        }

    }

    public void pause(int id) {
        saveWithStatus(id, DownloadInfo.STATUS_PAUSE);
    }

    public void pauseAll() {
        for(int i = 0, size = mDownloads.size(); i < size; i++) {
            int key = mDownloads.keyAt(i);
            pause(key);
        }
    }

    public void stop(int id) {
        saveWithStatus(id, DownloadInfo.STATUS_STOP);
    }

    public void stopAllByType(int type) {
        for (int i = 0; i < mDownloads.size(); ++i) {
            IDownloadTask task = mDownloads.valueAt(i);
            DownloadInfo info = task.getDownloadInfo();

            if (info != null && info.getType() == type) {
                stop(info.getId());
            }
        }
    }

    public void registerListener(DownloadListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void unregisterListener(DownloadListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    private void startDownloadTask(DownloadInfo info) {
        info.setStatus(DownloadInfo.STATUS_PENDING);
        int id = (int) mDownloadRepository.save(info);
        info.setId(id);

        if (id <= 0) {
            return;
        }

        IDownloadTask task;

        task = new DownloadTask(info, mDownloadRepository);
//        task = new FakeDownloadTask(info, mDownloadRepository);

        synchronized (mDownloads) {
            mDownloads.append(info.getId(), task);
        }

        startInterval();
        mDownloadManager.addTask(task);
        invokeListener(info);
    }

    private void startInterval() {
        if (mNotificationHandler == null) {
            HandlerThread thread = new HandlerThread("ProgressThread");
            thread.start();
            mNotificationHandler = new Handler(thread.getLooper());
            mNotificationHandler.postDelayed(mRunnable, BUSY_TIMER);
        }
    }

    private void invokeListener(DownloadInfo info) {
        for (DownloadListener listener : mListeners) {
            listener.progress(info);

            if (info.getStatus() == DownloadInfo.STATUS_COMPLETED) {
                listener.completed(info);
            }
        }
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }

    public interface DownloadListener {
        void progress(DownloadInfo info);
        void completed(DownloadInfo info);
    }
}
