package com.firefly.emulationstation.update;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.BaseDialogActivity;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Version;
import com.firefly.emulationstation.data.exceptions.UrlInvalidException;
import com.firefly.emulationstation.services.downloader.DownloadService;

public class UpdateDownloadDialog extends BaseDialogActivity {
    public static final String ARG_VERSION = "version";

    private Version mVersion;

    private Connection mConnection = new Connection();
    private DownloadListener mDownloadListener = new DownloadListener();
    private DownloadService mService;
    private DownloadInfo mDownloadInfo;
    private int mDownloadId = -1;

    private ProgressBar mProgressBar;
    private TextView mProgressTextView;
    private Button mRetryButton;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mProgressBar.setProgress(msg.arg1);
                    mProgressTextView.setText(getString(R.string.downloading_with_progress, msg.arg1));
                    break;
                case 2:
                    mRetryButton.setVisibility(View.VISIBLE);
                    mProgressTextView.setText(R.string.error);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_download_dialog);

        mVersion = (Version) getIntent().getSerializableExtra(ARG_VERSION);

        if (mVersion == null) {
            throw new IllegalArgumentException("Version can't be null");
        }

        findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnNewThread(new Runnable() {
                    @Override
                    public void run() {
                        mService.stop(mDownloadId);
                        finish();
                    }
                });
            }
        });
        mRetryButton = findViewById(R.id.retry_btn);
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnNewThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mService.download(mDownloadInfo);
                        } catch (UrlInvalidException e) {
                            e.printStackTrace();
                        }
                    }
                });
                mRetryButton.setVisibility(View.INVISIBLE);
            }
        });

        mProgressBar = findViewById(R.id.progress_bar);
        mProgressTextView = findViewById(R.id.progress_text);

    }

    @Override
    protected void onResume() {
        super.onResume();

        bindService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    private void bindService() {
        Intent intent = new Intent(this, DownloadService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private void tryToDownload() {
        try {
            if (mDownloadInfo == null) {
                mDownloadInfo = new DownloadInfo(
                        "emulationStation",
                        mVersion.getUrl(),
                        DownloadInfo.TYPE_APK,
                        Constants.UPDATE_APK,
                        mVersion.getVersion());
                mDownloadInfo.setRef(mVersion);
            }

            mDownloadId = mService.download(mDownloadInfo);
            mService.registerListener(mDownloadListener);
        } catch (UrlInvalidException e) {
            e.printStackTrace();
        }
    }

    private void runOnNewThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    private class Connection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((DownloadService.DownloadBinder)service).getService();

            runOnNewThread(new Runnable() {
                @Override
                public void run() {
                    tryToDownload();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService.unregisterListener(mDownloadListener);
            mService = null;
        }
    }

    private class DownloadListener implements DownloadService.DownloadListener {

        @Override
        public void progress(DownloadInfo info) {
            if (info.getId() == mDownloadId) {
                Message message = mHandler.obtainMessage();

                switch (info.getStatus()) {
                    case DownloadInfo.STATUS_DOWNLOADING:
                        message.what = 1;
                        break;
                    case DownloadInfo.STATUS_ERROR:
                        message.what = 2;
                        break;
                }

                message.arg1 = info.getShowProgress();
                mHandler.sendMessage(message);
            }
        }

        @Override
        public void completed(DownloadInfo info) {
            if (info.getId() == mDownloadId) {
                finish();
                // process in DownloadReceiver
            }
        }
    }
}
