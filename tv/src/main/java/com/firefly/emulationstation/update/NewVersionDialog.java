package com.firefly.emulationstation.update;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.BaseDialogActivity;
import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.bean.Version;
import com.firefly.emulationstation.data.repository.GamePlayRepository;
import com.firefly.emulationstation.data.repository.VersionRepository;
import com.firefly.emulationstation.settings.retroarch.RetroArchInfoActivity;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.commom.Constants.SETTINGS_IGNORE_VERSION;

public class NewVersionDialog extends BaseDialogActivity {
    public static final String ARG_WHICH_NEW_VERSION = "which_new_version";
    public static final String ARG_SHOW_IGNORE_CHECKBOX = "show_ignore_checkbox";

    public static final int RETROARCH_NEW_VERSION = 0;
    public static final int SELF_NEW_VERSION = 1;

    private TextView mInfo;
    private LinearLayout mLogParent;
    private TextView mLogView;

    private Version mVersion;

    @Inject
    GamePlayRepository mGamePlayRepository;
    @Inject
    VersionRepository mVersionRepository;
    @Inject
    SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_version_dialog);

        mInfo = findViewById(R.id.info);
        mLogParent = findViewById(R.id.release_log);
        mLogView = findViewById(R.id.release_log_view);

        final Button positiveButton = findViewById(R.id.positive_btn);
        final Button negativeButton = findViewById(R.id.negative_btn);

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View.OnClickListener listener = null;
        int which = getIntent().getIntExtra(ARG_WHICH_NEW_VERSION, -1);
        switch (which) {
            case RETROARCH_NEW_VERSION:
                getRetroArchVersionInfo();
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewVersionDialog.this, RetroArchInfoActivity.class);
                        intent.putExtra(RetroArchInfoActivity.ARG_UPDATE_FLAG, true);
                        startActivity(intent);
                        finish();
                    }
                };
                break;
            case SELF_NEW_VERSION:
                getSelfVersionInfo();
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(NewVersionDialog.this, UpdateDownloadDialog.class);
                        intent.putExtra(UpdateDownloadDialog.ARG_VERSION, mVersion);
                        startActivity(intent);
                        finish();
                    }
                };
                break;
            default:
                finish();
                break;
        }

        boolean showIgnoreBox = getIntent().getBooleanExtra(ARG_SHOW_IGNORE_CHECKBOX, false);
        if (showIgnoreBox) {
            CheckBox checkBox = findViewById(R.id.ignore_checkbox);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = mSettings.edit();
                    if (isChecked) {
                        positiveButton.setEnabled(false);
                        positiveButton.setFocusable(false);
                        editor.putInt(SETTINGS_IGNORE_VERSION, mVersion.getVersionCode());
                    } else {
                        positiveButton.setEnabled(true);
                        positiveButton.setFocusable(true);
                        editor.putInt(SETTINGS_IGNORE_VERSION, -1);
                    }
                    editor.apply();
                }
            });
        }

        positiveButton.setOnClickListener(listener);
    }

    private void getSelfVersionInfo() {
        Disposable disposable = mVersionRepository.getVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Version>() {
                    @Override
                    public void accept(Version version) throws Exception {
                        mVersion = version;

                        mInfo.setText(getString(R.string.self_have_new_version,
                                version.getVersion()));
                        if (!TextUtils.isEmpty(version.getLocalizeLog())) {
                            mLogParent.setVisibility(View.VISIBLE);
                            mLogView.setMovementMethod(new ScrollingMovementMethod());
                            mLogView.setText(version.getLocalizeLog());
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private void getRetroArchVersionInfo() {
        Disposable disposable = mGamePlayRepository.getGamePlays()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GamePlay>>() {
                    @Override
                    public void accept(List<GamePlay> gamePlays) throws Exception {
                        GamePlay retroarch = gamePlays.get(0);
                        mInfo.setText(getString(R.string.retroarch_have_new_version,
                                retroarch.getVersion()));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }
}
