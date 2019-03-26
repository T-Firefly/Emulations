package com.firefly.emulationstation;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.InputDevice;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.repository.DownloadRepository;
import com.firefly.emulationstation.guide.GuideActivity;
import com.firefly.emulationstation.inputmap.InputMapActivity;
import com.firefly.emulationstation.services.RetroArchDownloadService;
import com.firefly.emulationstation.services.downloader.DownloadService;
import com.firefly.emulationstation.utils.DbVersionUpgradeHelper;
import com.firefly.emulationstation.utils.ExternalStorageHelper;
import com.firefly.emulationstation.utils.GamePadHelper;
import com.firefly.emulationstation.utils.NetworkHelper;
import com.firefly.emulationstation.utils.RecommendedGameHelper;
import com.firefly.emulationstation.utils.Utils;

import java.util.List;

import javax.inject.Inject;

import dagger.android.DaggerActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends DaggerActivity {

    private static final int REQUEST_CODE_CHECK_PERMISSION = 1;
    private static final int REQUEST_CODE_ONBOARDING = 2;

    public static final int MSG_INITIALIZING = 1;
    public static final int MSG_FINISH = 2;
    public static final int MSG_UPDATE_RESOURCES = 3;

    @Inject
    ExternalStorageHelper mExternalStorageHelper;
    @Inject
    SharedPreferences mSettings;
    @Inject
    DbVersionUpgradeHelper mDbVersionUpgradeHelper;
    @Inject
    DownloadRepository mDownloadRepository;
    @Inject
    RecommendedGameHelper mRecommendedGameHelper;

    private TextSwitcher mInitialView;
    private Button mDownloadInBackBtn;
    private ProgressBar mProgressBar;
    private boolean mShowOnboarding;
    private boolean isFirstStart;
    private boolean isInstallRetroArch = false;

    private RetroArchDownloadService mDownloadService;
    private boolean mHasBindService = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RetroArchDownloadService.RetroArchDownloadBinder binder =
                    (RetroArchDownloadService.RetroArchDownloadBinder) service;

            mDownloadService = binder.getService();
            mDownloadService.setDownloadListener(new RetroArchDownloadService.DownloadListener() {
                @Override
                public void progress(GamePlay item, int progress) {}

                @Override
                public void progress(final int type, final int progress) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            if (type == 0) {
                                mInitialView.setCurrentText(
                                        getString(R.string.retroarch_downloading, progress));
                                if (progress == 100) {
                                    mInitialView.setText(getString(R.string.cores_downloading, 0));
                                }
                            } else {
                                mInitialView.setCurrentText(
                                        getString(R.string.cores_downloading, progress));
                            }
                        }
                    });
                }

                @Override
                public void completed() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            mInitialView.setText(getString(R.string.text_ready));
                            mDownloadInBackBtn.setText(R.string.text_enjoy);
                        }
                    });
                }
            });

            mDownloadService.start();
            mInitialView.setText(getString(R.string.retroarch_downloading, 0));
            mDownloadInBackBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownloadService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mInitialView = findViewById(R.id.initial);
        mDownloadInBackBtn = findViewById(R.id.btn_download_in_backgroud);
        mProgressBar = findViewById(R.id.progress);

        mInitialView.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // create a TextView
                TextView t = new TextView(SplashActivity.this);
                t.setTextAppearance(SplashActivity.this,
                        android.R.style.TextAppearance_Material_Large);
                t.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                return t;
            }
        });
        mInitialView.setInAnimation(this, R.anim.slide_in_up);
        mInitialView.setOutAnimation(this, R.anim.slide_out_up);

        mDownloadInBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkInputDevice();
            }
        });

        mShowOnboarding =
                mSettings.getBoolean(Constants.SETTINGS_SHOW_ONBOARDING, true);
        isFirstStart = mSettings.getBoolean(Constants.SETTINGS_FIRST_START, true);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {


            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

                showRequestPermissionRationale();

            } else {
                requestPermissions();
            }
        } else {
            showOnboardingView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHasBindService) {
            unbindService(mConnection);
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_CHECK_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_CHECK_PERMISSION) {
            return;
        }

        for (int i = 0; i < permissions.length; ++i) {
            if ((permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) &&
                    grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Utils.showToast(this, R.string.notice_for_request_permission);
                finish();
                return;
            }
        }

        showOnboardingView();
    }

    private void showOnboardingView() {
        if (mShowOnboarding) {
            startActivityForResult(
                    new Intent(this, GuideActivity.class),
                    REQUEST_CODE_ONBOARDING);
        } else {
            initExternalStorage();
        }
    }

    private void initExternalStorage() {
        // Try to update database.
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDbVersionUpgradeHelper.update();
            }
        }).start();

        Disposable disposable = mExternalStorageHelper.init()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        switch (integer) {
                            case MSG_UPDATE_RESOURCES:
                                mInitialView.setText(getString(R.string.update_resources));
                                mInitialView.setVisibility(View.VISIBLE);
                            case MSG_INITIALIZING:
                                mInitialView.setText(getString(R.string.initializing));
                                mInitialView.setVisibility(View.VISIBLE);
                                break;
                            case MSG_FINISH:
                                initialFinish();
                                break;
                            default:
                                break;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Utils.showToast(SplashActivity.this, R.string.external_storage_cant_write);
                    }
                });
    }

    private void initialFinish() {
        if (isFirstStart) {
            mRecommendedGameHelper.run();
        }

        if (isInstallRetroArch) {
            downloadRetroArch();
        } else {
            checkInputDevice();
        }
    }

    private void showRequestPermissionRationale() {
        PromptDialog dialog = new PromptDialog()
                .setTitle(R.string.browse_title)
                .setMessage(R.string.notice_for_request_permission)
                .setPositiveButton(R.string.ok, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog.show(getFragmentManager(), "Permission");
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                requestPermissions();
            }
        });
    }

    /*
     * Check for no config gamepad
     */
    private void checkInputDevice() {
        Disposable disposable = GamePadHelper.checkGamePad()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<InputDevice>>() {
                    @Override
                    public void accept(List<InputDevice> inputDevices) throws Exception {
                        if (inputDevices.isEmpty()) {
                            checkNotCompleteDownload();
                            return;
                        }

                        if (mSettings.getBoolean(Constants.SETTINGS_CHECK_GAMEPAD, true)) {
                            showConfigGamepadNotice(inputDevices);
                        } else {
                            checkNotCompleteDownload();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();

                    }
                });
    }

    private void checkNotCompleteDownload() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service :
                    manager.getRunningServices(Integer.MAX_VALUE)) {
                if (DownloadService.class.getName().equals(service.service.getClassName())) {
                    startMainActivity();
                    return;
                }
            }
        }

        Disposable disposable = mDownloadRepository.findWithGameByStatus(
                DownloadInfo.STATUS_DOWNLOADING, DownloadInfo.STATUS_PAUSE, DownloadInfo.STATUS_PENDING)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<DownloadInfo>>() {
                    @Override
                    public void accept(List<DownloadInfo> list) throws Exception {
                        if (!list.isEmpty()) {
                            showContinueDownloadNotice();
                        } else {
                            startMainActivity();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        if (throwable instanceof IllegalStateException) {
                            Utils.showToast(SplashActivity.this, R.string.database_verison_error);
                            SplashActivity.this.finish();
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        startMainActivity();
                    }
                });
    }

    private void startDownloadService() {
        Intent intent = new Intent(this, DownloadService.class);
        intent.setAction(DownloadService.ACTION_START_NOT_COMPLETE);

        startService(intent);
    }

    private void showContinueDownloadNotice() {
        PromptDialog dialog = PromptDialog.newInstance(
                getString(R.string.continue_download_rom_notice));
        dialog.setNegativeButton(android.R.string.no, new PromptDialog.OnClickListener() {
            @Override
            public void onClick(DialogFragment dialog, int which) {
                mDownloadRepository.updateDownloadingStatusToPause();
            }
        });
        dialog.setPositiveButton(android.R.string.yes, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        startDownloadService();
                        dialog.dismiss();
                    }
                });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                startMainActivity();
            }
        });
        dialog.show(getFragmentManager(), "continueDownloadFragment");
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showConfigGamepadNotice(final List<InputDevice> inputDevices) {
        PromptDialog dialog = new PromptDialog();
        dialog.setTitle(R.string.warning)
                .setMessage(R.string.has_no_config_gamepad)
                .setPositiveButton(android.R.string.yes,
                        new PromptDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogFragment dialog, int which) {
                                int[] deviceIds = new int[inputDevices.size()];
                                for (int i = 0; i < inputDevices.size(); ++i) {
                                    deviceIds[i] = inputDevices.get(i).getId();
                                }

                                Intent intent = new Intent(SplashActivity.this,
                                        InputMapActivity.class);
                                intent.putExtra(InputMapActivity.PARAMS_DEVICE_IDS, deviceIds);
                                startActivityForResult(intent, 0);
                            }
                        })
                .setNegativeButton(android.R.string.no,
                        new PromptDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogFragment dialog, int which) {
                                dialog.dismiss();
                                startMainActivity();
                            }
                        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                startMainActivity();
            }
        });
        dialog.show(getFragmentManager(), "Gamepad notice");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ONBOARDING) {
            boolean isInstallRA = true;
            boolean isRetroArchInstalled = Utils.isRetroArchInstalled(this);

            if (data != null) {
                isInstallRA = data.getBooleanExtra(GuideActivity.INSTALL_RETROARCH, true);
            }

            if (isInstallRA && !isRetroArchInstalled) {
                isInstallRetroArch = true;
                if (NetworkHelper.isNetworkAvailable(this)) {
                    initExternalStorage();
                } else {
                    showNoNetworkDialog();
                }
            } else {
                if (isRetroArchInstalled) {
                    Utils.showToast(this, R.string.retroarch_is_installed);
                }

                initExternalStorage();
            }
        } else {
            checkNotCompleteDownload();
        }
    }

    private void downloadRetroArch() {
        Intent serviceIntent = new Intent(this, RetroArchDownloadService.class);

        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);

        mHasBindService = true;
    }

    private void showNoNetworkDialog() {
        PromptDialog dialog = new PromptDialog();
        dialog.setMessage(R.string.network_is_not_available)
                .setPositiveButton(R.string.text_continue, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        if (NetworkHelper.isNetworkAvailable(SplashActivity.this)) {
                            initExternalStorage();
                        } else {
                            showNoNetworkDialog();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        initExternalStorage();
                        isInstallRetroArch = false;
                        Toast.makeText(SplashActivity.this,
                                R.string.retroarch_donot_install, Toast.LENGTH_LONG).show();
                    }
                });

        dialog.show(getFragmentManager(), "NoNetwork");
    }
}
