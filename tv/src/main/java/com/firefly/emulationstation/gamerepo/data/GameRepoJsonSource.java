package com.firefly.emulationstation.gamerepo.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.util.Log;
import android.webkit.URLUtil;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.exceptions.RepositoryAlreadyNewestException;
import com.firefly.emulationstation.data.exceptions.RepositoryDownloadErrorException;
import com.firefly.emulationstation.data.exceptions.RepositoryExistsException;
import com.firefly.emulationstation.data.exceptions.RepositoryInvalidException;
import com.firefly.emulationstation.data.exceptions.RepositoryNotSupportException;
import com.firefly.emulationstation.data.exceptions.UrlInvalidException;
import com.firefly.emulationstation.gamerepo.data.bean.Plat;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;
import com.firefly.emulationstation.gamerepo.data.bean.Rom;
import com.firefly.emulationstation.services.downloader.DownloadService;
import com.firefly.emulationstation.utils.ExternalStorageHelper;
import com.firefly.emulationstation.utils.Utils;
import com.firefly.emulationstation.utils.ZipHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.firefly.emulationstation.commom.Constants.CACHE_DIR;
import static com.firefly.emulationstation.commom.Constants.REPO_DIR;

/**
 * Created by rany on 18-4-24.
 */

public class GameRepoJsonSource implements IGameRepoSource {
    private static final String TAG = GameRepoJsonSource.class.getSimpleName();
    private static final String DEFAULT_REPO_FILE_NAME = "repo.json";

    private Context mContext;
    private DownloadService mDownloadService;
    private ServiceConnection mConnection;
    private ConditionVariable mTotalCondition;

    @Inject
    GameRepoJsonSource(Context context) {
        mContext = context;
    }

    @Override
    public Observable<Set<Repo>> repos() {
        return Observable.create(new ObservableOnSubscribe<Set<Repo>>() {
            @Override
            public void subscribe(ObservableEmitter<Set<Repo>> e) throws Exception {
                Set<Repo> result = getRepos();

                e.onNext(result);
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Repo> addOrUpdateRepo(final String path, final boolean isNew) {
        return Observable.create(new ObservableOnSubscribe<Repo>() {
            @Override
            public void subscribe(ObservableEmitter<Repo> e) throws Exception {
                if (path.startsWith("http://")
                        || path.startsWith("https://")
                        || path.startsWith("ftp://")) {
                    addOrUpdateNetworkRepo(e, path, isNew);
                } else if (path.endsWith(".zip")) {
                    addNewZipRepo(e, path);
                } else {
                    e.onError(new RepositoryNotSupportException());
                }

                e.onComplete();
            }
        });
    }

    @Override
    public Observable<Set<Repo>> deleteRepo(final Repo repo) {
        return Observable.create(new ObservableOnSubscribe<Set<Repo>>() {
            @Override
            public void subscribe(ObservableEmitter<Set<Repo>> e) throws Exception {
                File file = new File(Constants.REPO_DIR, repo.getId());
                ExternalStorageHelper.delete(file);
                e.onNext(getRepos());
            }
        });
    }

    @Override
    public Observable<List<Rom>> getRoms(final GameSystem gameSystem, final int filterStatus) {
        return Observable.create(new ObservableOnSubscribe<List<Rom>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Rom>> e) throws Exception {
                Set<Repo> repos = getRepos();
                List<Rom> roms = new ArrayList<>();
                String platformId = gameSystem.getPlatformId();

                for (Repo repo : repos) {
                    if (repo.isSupport(platformId)) {
                        File romsFile = new File(REPO_DIR,
                                repo.getId() + "/platforms/" + platformId + ".json");
                        try {
                            roms = Rom.fromFile(romsFile);

                            roms.remove(null);
                            for (Rom rom : roms) {
                                rom.setRepo(repo.getId());
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                e.onNext(roms);
                e.onComplete();
            }
        });
    }

    @Override
    public Observable<List<Rom>> getRomsFromRepo(final String repoId) {
        return Observable.create(new ObservableOnSubscribe<List<Rom>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Rom>> e) throws Exception {
                List<Rom> roms = new ArrayList<>();
                File repoFile = new File(Constants.REPO_DIR,
                        repoId + "/" + DEFAULT_REPO_FILE_NAME);
                Repo repo = Repo.fromFile(repoFile);

                for (Plat plat : repo.getPlatforms()) {
                    File romsFile = new File(REPO_DIR,
                            repo.getId() + "/platforms/" + plat.getPlatformId() + ".json");
                    List<Rom> platRoms = Rom.fromFile(romsFile);

                    platRoms.remove(null);
                    for (Rom rom : platRoms) {
                        rom.setRepo(repo.getId());
                    }

                    roms.addAll(platRoms);
                }

                e.onNext(roms);
            }
        });
    }

    private Set<Repo> getRepos() {
        File reposFile = new File(REPO_DIR);
        File[] repoFiles = reposFile.listFiles();

        Set<Repo> result = new HashSet<>();

        if (repoFiles == null) {
            return result;
        }

        for (File file : repoFiles) {
            if (file.isDirectory()) {
                File repoFile = new File(file, DEFAULT_REPO_FILE_NAME);
                if (!repoFile.exists())
                    continue;

                try {
                    Repo repo = Repo.fromFile(repoFile);

                    if (repo.selfCheck()) {
                        if (!result.add(repo)) {
                            Log.e(TAG, "The repository at \"" + repoFile.getCanonicalPath() + "\" " +
                                    "conflict with another, ignore.");
                        }
                    } else {
                        Log.e(TAG, "The file \"" + repoFile.getCanonicalPath() + "\" is invalid.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    private void addOrUpdateNetworkRepo(final ObservableEmitter<Repo> emitter,
                                        final String url,
                                        final boolean isNew) {
        if (!url.endsWith(".json")) {
            emitter.onError(new RepositoryInvalidException("The url of repository must end with .json"));
        }
        final ConditionVariable conditionVariable = new ConditionVariable();

        final DownloadService.DownloadListener listener = new DownloadService.DownloadListener() {
            private boolean flag = true;
            private volatile int count = 0;
            Repo repo = null;
            File cacheFile;

            @Override
            public void progress(DownloadInfo info) {
                if (info.getType() == DownloadInfo.TYPE_REPO
                        && info.getStatus() == DownloadInfo.STATUS_ERROR) {
                    emitter.onError(new RepositoryDownloadErrorException());
                    clearService(this);
                }
            }

            @Override
            public void completed(DownloadInfo info) {
                if (info.getType() == DownloadInfo.TYPE_REPO && flag) {
                    try {
                        File repoFile = new File(info.getPath());
                        repo = Repo.fromFile(repoFile);

                        if (repo.selfCheck()) {
                            File destFile = new File(Constants.REPO_DIR,
                                    repo.getId() + "/" + DEFAULT_REPO_FILE_NAME);
                            if (destFile.exists()) {
                                Repo oldRepo = Repo.fromFile(destFile);
                                if (oldRepo.getVersion().equals(repo.getVersion())) {
                                    if (isNew) {
                                        emitter.onError(new RepositoryExistsException());
                                    } else {
                                        emitter.onError(new RepositoryAlreadyNewestException());
                                    }
                                    clearService(this);
                                    return;
                                }
                            }

                            cacheFile = new File(Constants.CACHE_DIR, repo.getId());
                            String path = new File(cacheFile, "platforms")
                                    .getCanonicalPath();

                            cacheFile.mkdirs();
                            ExternalStorageHelper.copy(repoFile, new File(cacheFile, DEFAULT_REPO_FILE_NAME));
                            repoFile.delete();

                            for (Plat plat : repo.getPlatforms()) {
                                try {
                                    DownloadInfo downloadInfo = new DownloadInfo(
                                            repo.getId() + "_" + plat.getPlatformId(),
                                            plat.getUrl(),
                                            DownloadInfo.TYPE_REPO,
                                            path + "/" + plat.getPlatformId() + ".json",
                                            ""
                                    );
                                    mDownloadService.download(downloadInfo);
                                } catch (Throwable e) {
                                    emitter.onError(e);
                                    clearService(this);
                                    if (e instanceof UrlInvalidException){
                                        Utils.showToast(mContext, R.string.url_is_invalid);
                                    }
                                    break;
                                }
                                ++count;
                            }
                            flag = false;
                        } else {
                            emitter.onError(new RepositoryInvalidException());
                            clearService(this);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        emitter.onError(e);
                        clearService(this);
                    }
                } else if (info.getType() == DownloadInfo.TYPE_REPO) {
                    --count;
                    if (count == 0) {
                        try {
                            File repoFile = new File(Constants.REPO_DIR, repo.getId());
                            if (!isNew) {
                                ExternalStorageHelper.delete(repoFile);
                            }
                            ExternalStorageHelper.copyDir(cacheFile, repoFile);
                            ExternalStorageHelper.delete(cacheFile);
                            emitter.onNext(repo);
                        } catch (IOException e) {
                            emitter.onError(e);
                        }
                        clearService(this);
                    }
                }
            }
        };

        Intent intent = new Intent(mContext, DownloadService.class);
        mContext.bindService(intent, mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DownloadService.DownloadBinder binder = (DownloadService.DownloadBinder) service;
                mDownloadService = binder.getService();

                mDownloadService.registerListener(listener);
                conditionVariable.open();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);

        conditionVariable.block(10000);
        try {
            mDownloadService.download(createDownloadInfo(url, Constants.CACHE_DIR));
            mTotalCondition = new ConditionVariable();
            mTotalCondition.block();
        } catch (Throwable e1) {
            emitter.onError(e1);
            clearService(listener);
            if (e1 instanceof UrlInvalidException) {
                Utils.showToast(mContext, R.string.url_is_invalid);
            }
        }
    }

    private void clearService(DownloadService.DownloadListener listener) {
        mDownloadService.stopAllByType(DownloadInfo.TYPE_REPO);
        mDownloadService.unregisterListener(listener);
        mContext.unbindService(mConnection);
        mTotalCondition.open();
    }

    private DownloadInfo createDownloadInfo(String url, String pathPrefix) {
        String name = URLUtil.guessFileName(url, null, "application/json");
        String path = pathPrefix + "/" + name;

        return new DownloadInfo(
                name.substring(0, name.lastIndexOf('.')),
                url,
                DownloadInfo.TYPE_REPO,
                path,
                "");
    }

    private void addNewZipRepo(ObservableEmitter<Repo> e, String path) throws IOException {
        File outFile = null;
        InputStream inputStream = null;

        try {
            inputStream = obtainInputStream(path);
        } catch (FileNotFoundException ex) {
            e.onError(ex);
            e.onComplete();
        }

        outFile = new File(CACHE_DIR, "repoTmp");
        outFile.mkdirs();

        ZipHelper.decompress(inputStream, outFile);
        inputStream.close();

        if (!ZipHelper.isValidRepoPackage(outFile, "json")) {
            e.onError(new Throwable("Not a valid package."));
            ExternalStorageHelper.delete(outFile);
            return;
        }

        File repoFile = new File(outFile, DEFAULT_REPO_FILE_NAME);
        Repo repo = Repo.fromFile(repoFile);
        ExternalStorageHelper.delete(outFile);

        Set<Repo> repos = getRepos();

        if (!repos.add(repo)) {
            Log.e(TAG, "The repository \"" + repo.getId() + "\" " +
                    "already exists, ignore.");
            e.onError(new RepositoryExistsException());
            return;
        }

        // extract the package to the right path
        File destFile = new File(REPO_DIR, repo.getId());
        ZipHelper.decompress(obtainInputStream(path), destFile);

        e.onNext(repo);
    }

    private InputStream obtainInputStream(String path) throws FileNotFoundException {
        InputStream inputStream;

        if (path.startsWith("content://")) {
            Uri uri = Uri.parse(path);
            inputStream = mContext.getContentResolver().openInputStream(uri);
            assert inputStream != null;
        } else {
            File inFile = new File(path);

            if (!inFile.exists()) {
                throw new FileNotFoundException();
            }

            inputStream = new FileInputStream(inFile);
        }

        return inputStream;
    }
}
