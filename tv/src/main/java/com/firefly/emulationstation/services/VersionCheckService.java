package com.firefly.emulationstation.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.bean.Version;
import com.firefly.emulationstation.data.exceptions.RetroArchCanNotGetInfoException;
import com.firefly.emulationstation.data.repository.GamePlayRepository;
import com.firefly.emulationstation.data.repository.VersionRepository;
import com.firefly.emulationstation.update.NewVersionDialog;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static com.firefly.emulationstation.commom.Constants.RETROARCH_PACKAGE_NAME;
import static com.firefly.emulationstation.commom.Constants.SETTINGS_IGNORE_VERSION;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 */
public class VersionCheckService extends IntentService {
    private static final String TAG = VersionCheckService.class.getSimpleName();

    private static final String ACTION_CHECK_VERSION =
            "com.firefly.emulationstation.services.action.CHECK_VERSION";
    @Inject
    GamePlayRepository mGamePlayRepository;
    @Inject
    VersionRepository mVersionRepository;
    @Inject
    SharedPreferences mSettings;

    public VersionCheckService() {
        super("VersionCheckService");
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionCheckVersion(Context context) {
        Intent intent = new Intent(context, VersionCheckService.class);
        intent.setAction(ACTION_CHECK_VERSION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CHECK_VERSION.equals(action)) {
                handleActionCheckVersion();
            }
        }
    }

    /**
     * Handle action checkVersion in the provided background thread with the provided
     * parameters.
     */
    private void handleActionCheckVersion() {
        if (!handleSelfVersionCheck()) {
            handleRetroArchVersionCheck();
        }
    }

    private boolean handleSelfVersionCheck() {
        try {
            int ignoreVersion = mSettings.getInt(SETTINGS_IGNORE_VERSION, -1);
            Version version = mVersionRepository.getVersionDirectly();
            if (version != null && version.hasNewVersion()
                    && ignoreVersion != version.getVersionCode()) {
                showNewVersionNotice(NewVersionDialog.SELF_NEW_VERSION);
                return true;
            }
        } catch (Exception e) {
            Log.d(TAG, "Self version check error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        return false;
    }

    private void handleRetroArchVersionCheck() {
        try {
            List<GamePlay> gamePlays = mGamePlayRepository.getGamePlaysDirectly();
            PackageInfo pInfo = getPackageManager().getPackageInfo(RETROARCH_PACKAGE_NAME, 0);
            int version = pInfo.versionCode;

            if (version < gamePlays.get(0).getVersionCode()) {
                showNewVersionNotice(NewVersionDialog.RETROARCH_NEW_VERSION);
            }
        } catch (RetroArchCanNotGetInfoException
                | PackageManager.NameNotFoundException e) {
            Log.d(TAG, "RetroArch version check error: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void showNewVersionNotice(int which) {
        Intent intent = new Intent("com.firefly.emulationstation.NEW_RETROARCH_VERSION");
        intent.putExtra(NewVersionDialog.ARG_WHICH_NEW_VERSION, which);
        if (which == NewVersionDialog.SELF_NEW_VERSION) {
            intent.putExtra(NewVersionDialog.ARG_SHOW_IGNORE_CHECKBOX, true);
        }

        startActivity(intent);
    }
}
