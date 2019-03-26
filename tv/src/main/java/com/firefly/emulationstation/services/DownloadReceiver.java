package com.firefly.emulationstation.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.bean.Version;
import com.firefly.emulationstation.data.repository.GameRepository;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.utils.Utils;
import com.firefly.emulationstation.utils.ZipHelper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.utils.Utils.findRom;

public class DownloadReceiver extends BroadcastReceiver {

    @Inject
    SystemsRepository mSystemsRepository;
    @Inject
    GameRepository mGameRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        if (Constants.BROADCAST_DOWNLOAD_COMPLETED.equals(intent.getAction())) {
            processCompleted(context, intent);
        } else if (Constants.BROADCAST_DOWNLOAD_ERROR.equals(intent.getAction())) {
            processError(context, intent);
        }
    }

    private void processCompleted(final Context context, final Intent intent) {
        final DownloadInfo info = (DownloadInfo) intent.getSerializableExtra("info");
        if (info == null) {
            return;
        }

        switch (info.getType()) {
            case DownloadInfo.TYPE_ROM:
                processRom(context, intent, info);
                break;
            case DownloadInfo.TYPE_APK:
                if (info.getRef() instanceof Version) {
                    Utils.installApk(context, new File(info.getPath()));
                }
                break;
        }
    }

    private void processRom(final Context context, final Intent intent, final DownloadInfo info) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Game game = (Game) info.getRef();
                if (game == null) {
                    return;
                }

                final List<String> systems = mGameRepository.findSupportSystems(game.getId());
                final Set<String> extensions = new HashSet<>();

                for (String system : systems) {
                    GameSystem gameSystem = mSystemsRepository.getGameSystem(system);
                    extensions.addAll(Arrays.asList(gameSystem.getExtensions()));
                }

                boolean support = false;
                String romPath = info.getPath();

                for (String ext : extensions) {
                    if (romPath.endsWith(ext)) {
                        support = true;
                        break;
                    }
                }

                if (!support && romPath.endsWith(".zip")) {
                    File zipFile = new File(romPath);
                    File romParentFile = null;
                    String outPath;
                    String wrapDir;

                    if ((wrapDir = ZipHelper.getWrapWithDir(zipFile)) != null) {
                        outPath = zipFile.getParent();
                        romParentFile = new File(outPath, wrapDir);
                    } else {
                        outPath = romPath.replace(".zip", "");
                    }

                    File zipOutFile = new File(outPath);
                    zipOutFile.mkdirs();
                    try {
                        ZipHelper.decompress(zipFile, zipOutFile);
                        if (romParentFile == null) {
                            romParentFile = zipOutFile;
                        } else {
                            File fileName = new File(romPath.replace(".zip", ""));
                            if (!fileName.exists() && romParentFile.renameTo(fileName)) {
                                romParentFile = fileName;
                            }
                        }

                        File romFile = new File(romParentFile, game.getName() + game.getExt());
                        if (romFile.exists()) {
                            romPath = romFile.getCanonicalPath();
                        } else {
                            String path = findRom(romParentFile, extensions);
                            if (path != null) {
                                romPath = path;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast(context,
                                        context.getString(R.string.rom_extract_eror, game.getDisplayName()));
                            }
                        });

                        return;
                    }
                }

                game.setPath(romPath);
                game.setExt(romPath.substring(romPath.lastIndexOf(".")));
                game.setStatus(Game.STATUS_NEW_GAME);
                Disposable disposable = mGameRepository.updateGame(game)
                        .subscribeOn(Schedulers.io())
                        .unsubscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean aBoolean) throws Exception {
                                Utils.showToast(context,
                                        context.getString(R.string.rom_download_completed, game.getDisplayName()));
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        });
            }
        }).start();
    }

    private void processError(Context context, Intent intent) {
        DownloadInfo info = (DownloadInfo) intent.getSerializableExtra("info");

        if (info == null || info.getType() != DownloadInfo.TYPE_ROM) {
            return;
        }
        Game game = (Game) info.getRef();

        Utils.showToast(context,
                context.getString(R.string.rom_download_error, game.getDisplayName()));
    }
}
