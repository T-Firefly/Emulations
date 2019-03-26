package com.firefly.emulationstation.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.exceptions.UrlInvalidException;
import com.firefly.emulationstation.data.repository.GamePlayRepository;
import com.firefly.emulationstation.services.downloader.DownloadService;
import com.firefly.emulationstation.utils.Utils;
import com.firefly.emulationstation.utils.ZipHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 */
public class RetroArchDownloadService extends Service {
    private static final int STATUS_DOWNLOADING = 0;
    private static final int STATUS_COMPLETED = 1;
    private static final int STATUS_PENDING = 2;

    private static final int FLAG_DOWNLOAD_APK = 1;
    private static final int FLAG_DOWNLOAD_CORES = 1 << 1;
    private static final int FLAG_DOWNLOAD_MISSING_ONLY = 1 << 2;

    private List<GamePlay> mGamePlays;
    private DownloadService mService;
    private DownloadListener mRListener;
    private RetroArchDownloadBinder mBinder = new RetroArchDownloadBinder();

    private ConditionVariable mCV;
    private ConditionVariable mServiceCV;
    private SparseArray<GamePlay> mItems = new SparseArray<>();
    private SparseArray<GamePlay> mCores = new SparseArray<>();
    private int mDownloadCount = 0;

    private BroadcastReceiver mPackageRemovedReceiver;

    @Inject
    GamePlayRepository mGamePlayRepository;

    private int mStatus = STATUS_PENDING;

    private DownloadService.DownloadListener mListener = new DownloadService.DownloadListener() {
        private int coreDownloadRepeatCount = 0;
        private int type = 0;
        private int totalProgress = 0;
        private int completed = 0;

        @Override
        public void progress(DownloadInfo info) {
            if (mRListener != null) {
                mRListener.progress(mItems.get(info.getId()), info.getShowProgress());
            }

            switch (info.getType()) {
                case DownloadInfo.TYPE_APK:
                    if (!(info.getRef() instanceof GamePlay)) {
                        break;
                    }

                    if (mRListener != null) {
                        mRListener.progress(type, info.getShowProgress());
                    }

                    if (info.getStatus() == DownloadInfo.STATUS_ERROR) {
                        ++completed;

                        if (completed == mDownloadCount) {
                            RetroArchDownloadService.this.completed();
                        }
                    }
                    break;
                case DownloadInfo.TYPE_CORE:
                    int size = mCores.size();
                    if (size <= 0) {
                        break;
                    }

                    coreDownloadRepeatCount = (++coreDownloadRepeatCount) % size ;
                    totalProgress += info.getShowProgress();

                    if (type == 0) {
                        break;
                    }

                    if (coreDownloadRepeatCount == 0) {
                        int progress = totalProgress / size;
                        if (mRListener != null && progress <= 100) {
                            mRListener.progress(type, progress);
                        }
                        totalProgress = 0;
                    }

                    if (info.getStatus() == DownloadInfo.STATUS_ERROR) {
                        ++completed;
                        if (completed == mDownloadCount) {
                            RetroArchDownloadService.this.completed();
                        }

                        mCores.remove(info.getId());
                        if (coreDownloadRepeatCount > 0)
                            --coreDownloadRepeatCount;
                    }

                    break;
            }
        }

        @Override
        public void completed(DownloadInfo info) {
            switch (info.getType()) {
                case DownloadInfo.TYPE_APK:
                    type = 1;
                    ++completed;
                    tryInstallRetroArch(mItems.get(info.getId()));
                    break;
                case DownloadInfo.TYPE_CORE:
                    ++completed;
                    final String name = info.getName();

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showToast(getApplicationContext(),
                                    getString(R.string.core_downloaded, name));
                        }
                    });
                    GamePlay item = mCores.get(info.getId());
                    mCores.remove(info.getId());

                    if (item != null && item.isCompress()) {
                        File zipFile = new File(info.getPath());
                        try {
                            ZipHelper.decompress(zipFile, zipFile.getParentFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }


            if (completed == mDownloadCount) {
                RetroArchDownloadService.this.completed();
            }
        }
    };

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
            mService = binder.getService();

            mServiceCV.open();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    public RetroArchDownloadService() {

    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();

        mCV = new ConditionVariable();
        mServiceCV = new ConditionVariable();

        initData();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mStatus == STATUS_PENDING) {
            Intent downloadIntent = new Intent(this, DownloadService.class);
            bindService(downloadIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        startService(intent);

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mRListener = null;

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mService != null) {
            mService.unregisterListener(mListener);
        }
        unbindService(mConnection);

        if (mPackageRemovedReceiver != null)
            unregisterReceiver(mPackageRemovedReceiver);
    }

    private void initData() {
        Disposable disposable = mGamePlayRepository.getGamePlays()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<GamePlay>>() {
                    @Override
                    public void accept(List<GamePlay> gamePlays) throws Exception {
                        mGamePlays = gamePlays;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        mCV.open();
                    }
                });
    }

    public void setDownloadListener(DownloadListener listener) {
        mRListener = listener;
    }

    public void start() {
        start(true, true, false);
    }

    public void startDownloadApk() {
        start(true, false, false);
    }

    public void startDownloadCores(boolean missingOnly) {
        start(false, true, missingOnly);
    }

    public void start(boolean downloadApk, boolean downloadCores,
                      boolean missingOnly) {
        int downloadFlag = 0;
        if (mStatus == STATUS_COMPLETED) {
            mStatus = STATUS_PENDING;
        }


        if (downloadApk) {
            downloadFlag |= FLAG_DOWNLOAD_APK;
        }
        if (downloadCores) {
            downloadFlag |= FLAG_DOWNLOAD_CORES;
        }
        if (missingOnly) {
            downloadFlag |= FLAG_DOWNLOAD_MISSING_ONLY;
        }

        if (mStatus == STATUS_PENDING) {
            final int finalDownloadFlag = downloadFlag;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mGamePlays == null) {
                        initData();
                        mCV.block();
                    }
                    if (mService == null) {
                        mServiceCV.block();
                    }

                    startDownload(finalDownloadFlag);
                }
            }).start();
        }
    }

    public boolean isDownloading() {
        return mStatus == STATUS_DOWNLOADING;
    }

    public void tryInstallRetroArch(GamePlay retroarch) {
        try {
            if (retroarch.isCleanInstall() && Utils.isRetroArchInstalled(this)) {
                mPackageRemovedReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (!Constants.RETROARCH_PACKAGE_NAME
                                .equals(intent.getData().getSchemeSpecificPart())) {
                            return;
                        }

                        Utils.installApk(context, new File(Constants.RETROARCH_APK));
                        unregisterReceiver(mPackageRemovedReceiver);
                        mPackageRemovedReceiver = null;

                        if (mStatus == STATUS_COMPLETED) {
                            stopSelf();
                        }
                    }
                };
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                intentFilter.addDataScheme("package");

                registerReceiver(mPackageRemovedReceiver, intentFilter);

                Utils.uninstallApp(getApplicationContext(), Constants.RETROARCH_PACKAGE_NAME);
            } else {
                Utils.installApk(getApplicationContext(), new File(Constants.RETROARCH_APK));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startDownload(int downloadFlag) {
        if (mGamePlays == null) {
            completed();
            return;
        }


        int id = 0;
        try {
            if ((downloadFlag & FLAG_DOWNLOAD_APK) == 1) {
                GamePlay retroarch = mGamePlays.get(0);
                DownloadInfo downloadInfo = new DownloadInfo(
                        "retroarch",
                        retroarch.getUrl(),
                        DownloadInfo.TYPE_APK,
                        Constants.RETROARCH_APK,
                        retroarch.getVersion());
                downloadInfo.setRef(retroarch);

                id = mService.download(downloadInfo);
                mItems.put(id, retroarch);
                ++mDownloadCount;
            }

            if ((downloadFlag & FLAG_DOWNLOAD_CORES) != 0) {
                for (int i = 1; i < mGamePlays.size(); ++i) {
                    GamePlay item = mGamePlays.get(i);
                    File file = new File(Constants.CORES_DIR, item.getFileName());

                    if ((downloadFlag & FLAG_DOWNLOAD_MISSING_ONLY) == 0 || !file.exists()) {
                        id = mService.download(new DownloadInfo(item.getFileName(),
                                item.getUrl(),
                                DownloadInfo.TYPE_CORE,
                                Constants.CORES_DIR + "/" + item.getFileName(),
                                item.getVersion()));
                        mItems.put(id, item);
                        mCores.put(id, item);
                        ++mDownloadCount;
                    }
                }
            }

            if (mDownloadCount != 0) {
                mService.registerListener(mListener);
                mStatus = STATUS_DOWNLOADING;
            }
        } catch (Exception e) {
            e.printStackTrace();
            completed();
        } catch (UrlInvalidException e) {
            Utils.showToast(getApplicationContext(), R.string.url_is_invalid);
        }
    }

    private void completed() {
        if (mRListener != null) {
            mRListener.completed();
        }
        mStatus = STATUS_COMPLETED;

        if (mPackageRemovedReceiver == null)
            stopSelf();
    }

    public class RetroArchDownloadBinder extends Binder {
        public RetroArchDownloadService getService() {
            return RetroArchDownloadService.this;
        }
    }

    public interface DownloadListener {
        void progress(GamePlay gamePlay, int progress);
        void progress(int type, int progress);
        void completed();
    }
}
