package com.firefly.emulationstation.settings.retroarch;


import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.app.GuidedStepSupportFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.view.View;
import android.view.ViewGroup;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.commom.fragment.PromptDialog;
import com.firefly.emulationstation.commom.view.ProgressDialog;
import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.repository.GamePlayRepository;
import com.firefly.emulationstation.services.RetroArchDownloadService;
import com.firefly.emulationstation.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A {@link GuidedStepSupportFragment} subclass.
 */
public class InfoFragment extends GuidedStepFragment
        implements RetroArchDownloadService.DownloadListener {
    private static final String TAG = InfoFragment.class.getSimpleName();

    private final static long ACTION_DOWNLOAD_ALL = 1;
    private final static long ACTION_DOWNLOAD_RETROARCH = 2;
    private final static long ACTION_DOWNLOAD_ALL_CORE = 3;
    private final static long ACTION_DOWNLOAD_MISSING_CORES = 4;
    private final static long ACTION_BACK = 5;
    private final static long ACTION_INSTALL_RETROARCH = 6;

    private VerticalGridPresenter mGridPresenter;
    private VerticalGridPresenter.ViewHolder mGridViewHolder;
    private ArrayObjectAdapter mRowAdapter;
    private int mSelectedPosition = -1;

    private RetroArchDownloadService mService;
    private Map<String, Integer> mItemMap = new HashMap<>();
    private GamePlay mRetroArchBean;
    private ProgressDialog mProgressDialog;

    @Inject
    GamePlayRepository mGamePlayRepository;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RetroArchDownloadService.RetroArchDownloadBinder binder =
                    (RetroArchDownloadService.RetroArchDownloadBinder) service;
            mService = binder.getService();

            mService.setDownloadListener(InfoFragment.this);

            if (getActivity().getIntent()
                    .getBooleanExtra(RetroArchInfoActivity.ARG_UPDATE_FLAG, false)) {
                getActivity().getIntent().removeExtra(RetroArchInfoActivity.ARG_UPDATE_FLAG);
                mService.startDownloadApk();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public InfoFragment() {
        mGridPresenter = new VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_NONE);
        mGridPresenter.setNumberOfColumns(1);

        WideCardPresenter cardPresenter = new WideCardPresenter();
        mRowAdapter = new ArrayObjectAdapter(cardPresenter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mProgressDialog = ProgressDialog.newInstance(getString(R.string.loading));
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent serviceIntent = new Intent(getActivity(), RetroArchDownloadService.class);
        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup dock = view.findViewById(R.id.dock);
        mGridViewHolder = mGridPresenter.onCreateViewHolder(dock);

        VerticalGridView gridView = mGridViewHolder.getGridView();
        gridView.setColumnWidth(0);
        gridView.setPadding(
                0,
                gridView.getPaddingTop(),
                0,
                gridView.getPaddingBottom());
        gridView.getLayoutManager();
        dock.addView(mGridViewHolder.view);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSelectedPosition == -1) {
            mSelectedPosition = 0;
        }

        if (mService != null) {
            mService.setDownloadListener(this);
        }

        mProgressDialog.show(getFragmentManager(), "loading");
        Disposable disposable = mGamePlayRepository.getGamePlays()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GamePlay>>() {
                    @Override
                    public void accept(List<GamePlay> gamePlays) throws Exception {
                        mRowAdapter.clear();
                        mItemMap.clear();

                        mRetroArchBean = gamePlays.get(0);
                        for (int i = 0; i < gamePlays.size(); ++i) {
                            GamePlay item = gamePlays.get(i);

                            String status = getString(R.string.not_exists);
                            if (i == 0) {
                                if (Utils.isRetroArchInstalled(getActivity())) {
                                    status = getString(R.string.installed);
                                } else if (isRetroArchApkExists()) {
                                    status = getString(R.string.exists);
                                }
                            } else if (isCoreExists(item.getName())) {
                                status = getString(R.string.exists);
                            }

                            WideCardPresenter.Item data = new WideCardPresenter.Item(item.getName(), status);

                            mRowAdapter.add(data);
                            mItemMap.put(data.name, mRowAdapter.size() - 1);
                        }

                        createActions();
                        updateAdapter();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Utils.showToast(getActivity(), R.string.can_not_get_retroarch_info);
                        finishGuidedStepFragments();
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        mProgressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();

        mService.setDownloadListener(null);
    }

    @Override
    public void onStop() {
        super.onStop();

        getActivity().unbindService(mConnection);
    }

    @Override
    public GuidanceStylist onCreateGuidanceStylist() {
        return new GuidanceStylist() {
            @Override
            public int onProvideLayoutId() {
                return R.layout.fragment_retro_arch_info;
            }
        };
    }

    private void updateAdapter() {
        if (mRowAdapter != null) {
            mGridPresenter.onBindViewHolder(mGridViewHolder, mRowAdapter);

            if (mSelectedPosition != -1) {
                mGridViewHolder.getGridView().setSelectedPosition(mSelectedPosition);
            }
        }

    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (mService.isDownloading() && ACTION_BACK != action.getId()) {
            Utils.showToast(getActivity(), R.string.downloading_wait_until_complete);
        } else if (ACTION_DOWNLOAD_ALL == action.getId()) {
            if (isRetroArchApkExists() || isCoresExists(true)) {
                showDialog(getString(R.string.download_all_notice),
                        new PromptDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogFragment dialog, int which) {
                        if (which == -1) {
                            mService.start();
                        }
                    }
                });
            } else if (mService != null) {
                mService.start();
            }
        } else if (ACTION_DOWNLOAD_RETROARCH == action.getId()) {
            if (isRetroArchApkExists()) {
                showDialog(getString(R.string.download_apk_notice),
                        new PromptDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogFragment dialog, int which) {
                                if (which == -1) {
                                    mService.startDownloadApk();
                                }
                            }
                        });
            } else if (mService != null) {
                mService.startDownloadApk();
            }
        } else if (ACTION_DOWNLOAD_ALL_CORE == action.getId()) {
            if (isCoresExists(true)) {
                showDialog(getString(R.string.download_all_cores_notice),
                        new PromptDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogFragment dialog, int which) {
                                if (which == -1) {
                                    mService.startDownloadCores(false);
                                }
                            }
                        });
            } else if (mService != null) {
                mService.startDownloadCores(false);
            }
        } else if (ACTION_DOWNLOAD_MISSING_CORES == action.getId()) {
            mService.startDownloadCores(true);
        } else if (ACTION_INSTALL_RETROARCH == action.getId()) {
            mService.tryInstallRetroArch(mRetroArchBean);
        } else {
            getActivity().finish();
        }
    }

    private void showDialog(String msg, PromptDialog.OnClickListener listener) {
        new PromptDialog()
                .setMessage(msg)
                .setPositiveButton(android.R.string.yes, listener)
                .setNegativeButton(android.R.string.no, listener)
                .show(getFragmentManager(), "Dialog");
    }

    private void createActions() {
        List<GuidedAction> guidedActions = new ArrayList<>();
        GuidedAction downAllAction = new GuidedAction.Builder(getActivity())
                .id(ACTION_DOWNLOAD_ALL)
                .title(R.string.download_and_install)
                .editable(false)
                .build();
        GuidedAction downRetroAction = new GuidedAction.Builder(getActivity())
                .id(ACTION_DOWNLOAD_RETROARCH)
                .title(R.string.download_retroarch)
                .editable(false)
                .build();
        GuidedAction inRetroAction = new GuidedAction.Builder(getActivity())
                .id(ACTION_INSTALL_RETROARCH)
                .title(R.string.text_install_retroarch)
                .editable(false)
                .build();
        GuidedAction downCoreAction = new GuidedAction.Builder(getActivity())
                .id(ACTION_DOWNLOAD_ALL_CORE)
                .title(R.string.download_all_cores)
                .editable(false)
                .build();
        GuidedAction downMissingAction = new GuidedAction.Builder(getActivity())
                .id(ACTION_DOWNLOAD_MISSING_CORES)
                .title(R.string.download_missing_cores)
                .editable(false)
                .build();
        GuidedAction backAction = new GuidedAction.Builder(getActivity())
                .id(ACTION_BACK)
                .title(R.string.back)
                .editable(false)
                .build();

        guidedActions.add(downAllAction);
        guidedActions.add(downRetroAction);

        if (!Utils.isRetroArchInstalled(getActivity())) {
            guidedActions.add(inRetroAction);
        }

        guidedActions.add(downCoreAction);

        if (!isCoresExists(false)) {
            guidedActions.add(downMissingAction);
        }

        guidedActions.add(backAction);

        setActions(guidedActions);
    }

    private boolean isRetroArchApkExists() {
        return new File(Constants.RETROARCH_APK).exists();
    }

    private boolean isCoreExists(String name) {
        return new File(Constants.CORES_DIR, name).exists();
    }

    private boolean isCoresExists(boolean partExists) {
        for (int i = 1; i < mRowAdapter.size(); ++i) {
            WideCardPresenter.Item item = (WideCardPresenter.Item) mRowAdapter.get(i);

            if (partExists && isCoreExists(item.name)) {
                return true;
            } else if (!partExists && !isCoreExists(item.name)) {
                return false;
            }
        }

        return !partExists;
    }

    @Override
    public void progress(GamePlay item, int progress) {
        if (item == null) {
            return;
        }

        int index = mItemMap.get(item.getName());
        WideCardPresenter.Item data = (WideCardPresenter.Item) mRowAdapter.get(index);
        data.progress = progress;

        if (progress == 100) {
            data.status = getString(R.string.exists);
        } else {
            data.status = getString(R.string.downloading);
        }

        try {
            mRowAdapter.notifyArrayItemRangeChanged(index, 1);
        } catch (Exception ignore) {
        }
    }

    @Override
    public void progress(int type, int progress) { /* Not implement */ }

    @Override
    public void completed() {
    }
}
