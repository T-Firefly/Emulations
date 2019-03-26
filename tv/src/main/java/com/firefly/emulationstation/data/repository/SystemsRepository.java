package com.firefly.emulationstation.data.repository;

import android.content.Context;
import android.os.Build;
import android.os.storage.StorageManager;
import android.support.annotation.NonNull;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.GameRomPath;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.local.SystemsSource;
import com.firefly.emulationstation.data.local.db.GameRomPathDao;
import com.firefly.emulationstation.utils.StorageHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.commom.Constants.ROM_PATH_DEVICE_PREFIX;

/**
 * Created by rany on 17-10-31.
 */

@Singleton
public class SystemsRepository {
    private SystemsSource mSystemsSource;
    private GameRomPathDao mGameRomPathDao;
    private Context mContext;

    private File mDefaultFile = new File(Constants.ES_DIR, "systems.xml");
    private File mCustomFile = new File(Constants.ES_DIR, "systems-custom.xml");

    private static List<GameSystem> sGameSystems;

    @Inject
    public SystemsRepository(SystemsSource systemsSource,
                             GameRomPathDao gameRomPathDao,
                             Context context) {
        mSystemsSource = systemsSource;
        mGameRomPathDao = gameRomPathDao;
        mContext = context;
    }

    public Observable<List<GameSystem>> getGameSystems(final boolean refresh) {

        return Observable.create(new ObservableOnSubscribe<List<GameSystem>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GameSystem>> e) throws Exception {
                if (sGameSystems == null || refresh) {
                    getGameSystemsFromFile();
                }

                e.onNext(sGameSystems);

                if (refresh) {
                    StorageManager storageManager = (StorageManager) mContext
                            .getSystemService(Context.STORAGE_SERVICE);
                    String[] devices;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        devices = StorageHelper.getVolumePaths(storageManager);
                    } else {
                        devices = StorageHelper.getVolumePathsPreN(storageManager);
                    }

                    for (GameSystem gameSystem : sGameSystems) {
                        String path = gameSystem.getRomPath();
                        if (path.startsWith(ROM_PATH_DEVICE_PREFIX)) {
                            for (String devicePath : devices) {
                                String realPath = path.replace(ROM_PATH_DEVICE_PREFIX, devicePath);
                                saveRomPath(gameSystem, realPath);
                            }
                        } else {
                            saveRomPath(gameSystem, path);
                        }
                    }
                }

                e.onComplete();
            }
        });

    }

    public Observable<List<GameSystem>> getEnableGameSystem(boolean refresh) {
        return getGameSystems(refresh)
                .map(new Function<List<GameSystem>, List<GameSystem>>() {
                    @Override
                    public List<GameSystem> apply(List<GameSystem> gameSystems)
                            throws Exception {
                        List<GameSystem> result = new ArrayList<>();

                        for (GameSystem gameSystem : gameSystems) {
                            if (gameSystem.isEnable()) {
                                result.add(gameSystem);
                            }
                        }

                        return result;
                    }
                });
    }

    private void getGameSystemsFromFile() throws Exception {
        List<GameSystem> innerGameSystem =
                mSystemsSource.getGameSystems(mDefaultFile);
        List<GameSystem> customGameSystem = null;

        sGameSystems = innerGameSystem;
        // TODO: define a Exception to identify custom file error
        try {
            customGameSystem = mSystemsSource.getGameSystems(mCustomFile);
            mergeGameSystems(sGameSystems, customGameSystem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveRomPath(GameSystem gameSystem, String path) {
        final GameRomPath gameRomPath = new GameRomPath();
        gameRomPath.setPath(path);
        gameRomPath.setRomPathId(gameSystem.getRomPathID());

        mGameRomPathDao.save(gameRomPath);
    }

    public List<GameSystem> getMatchGameSystems(@NonNull String romPathId, String ext) {
        List<GameSystem> result = new ArrayList<>();

        for (GameSystem system : sGameSystems) {
            List<String> exts = Arrays.asList(system.getExtensions());
            if (system.isEnable()
                    && romPathId.equals(system.getRomPathID())
                    && exts.contains(ext)) {
                result.add(system);
            }
        }

        return result;
    }

    public GameSystem getGameSystem(@NonNull String name) {
        if (sGameSystems == null) {
            try {
                getGameSystemsFromFile();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        for (GameSystem gameSystem : sGameSystems) {
            if (name.equals(gameSystem.getName())) {
                return gameSystem;
            }
        }

        return null;
    }

    /**
     * Auto save to separate systems file
     */
    public void saveGameSystems() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<GameSystem> innerGameSystem = null;
                List<GameSystem> customGameSystem = null;
                try {
                    innerGameSystem = mSystemsSource.getGameSystems(mDefaultFile);
                    customGameSystem = mSystemsSource.getGameSystems(mCustomFile);

                    updateSystems(innerGameSystem, mDefaultFile);
                    updateSystems(customGameSystem, mCustomFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateSystems(List<GameSystem> target, File out) {
        for (GameSystem gs : target) {
            for (GameSystem system : sGameSystems) {
                if (gs.getName().equals(system.getName())) {
                    gs.setEnable(system.isEnable());
                    break;
                }
            }
        }

        saveGameSystems(target, out);
    }

    /**
     * Save game systems
     * @param gameSystems Game systems which want to save
     * @param out The File out which game systems will save to.
     */
    public void saveGameSystems(final List<GameSystem> gameSystems, final File out) {
        Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        mSystemsSource.saveGameSystem(gameSystems, out);
                    }
                })
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    /**
     * Merge custom system to inner system
     * @param dest inner system, default in app
     * @param src need to merge systems, aways the custom systems
     */
    private void mergeGameSystems(List<GameSystem> dest, List<GameSystem> src) throws Exception {
        GameSystem destSystem;
        for (GameSystem system : src) {
            if ((destSystem = getGameSystem(system.getName())) != null) {
                destSystem.merge(system);
            } else {
                dest.add(system);
            }
        }

    }

    /**
     * Get the default GameSystem from systems.xml
     * @return GameSystem list
     * @throws Exception
     */
    public List<GameSystem> getDefaultGameSystem() throws Exception {
        return mSystemsSource.getGameSystems(mDefaultFile);
    }
}
