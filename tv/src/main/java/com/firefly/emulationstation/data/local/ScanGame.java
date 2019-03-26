package com.firefly.emulationstation.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameRomPath;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.bean.GameSystemRef;
import com.firefly.emulationstation.data.local.db.Converters;
import com.firefly.emulationstation.data.local.db.GameDao;
import com.firefly.emulationstation.data.local.db.GameRomPathDao;
import com.firefly.emulationstation.data.local.db.GameSystemRefDao;
import com.firefly.emulationstation.utils.DateHelper;
import com.firefly.emulationstation.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;
import io.reactivex.MaybeSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.firefly.emulationstation.commom.Constants.SETTINGS_DELETE_NOT_EXISTS_ROM;


/**
 * Created by rany on 17-10-26.
 */

public class ScanGame {
    private static final String TAG = ScanGame.class.getSimpleName();

    private Context mContext;
    private GameNameFromMapFile mGameNameFromFile;
    private GameDao mGameDao;
    private GameRomPathDao mGameRomPathDao;
    private GameSystemRefDao mGameSystemRefDao;
    private GameSystem mGameSystem;
    private SharedPreferences mSettings;

    FileFilter mFileFilter;
    List<Game> mResult;

    public static List<String> ARCADE_BIOS = new ArrayList<String>() {{
        add("neogeo");
        add("pgm");
        add("qsound");
    }};

    public ScanGame(Context context,
                    GameDao gameDao,
                    GameRomPathDao gameRomPathDao,
                    GameSystemRefDao gameSystemRefDao,
                    SharedPreferences settings) {
        mContext = context;
        mGameDao = gameDao;
        mGameRomPathDao = gameRomPathDao;
        mGameSystemRefDao = gameSystemRefDao;
        mSettings = settings;

        mGameNameFromFile = new GameNameFromMapFile(mContext);
    }

    public ScanGame setFileFilter(FileFilter fileFilter) {
        mFileFilter = fileFilter;

        return this;
    }

    public Maybe<List<Game>> scan(@NonNull final GameSystem gameSystem) {
        mGameSystem = gameSystem;
        mResult = new ArrayList<>();

        return Maybe.create(new MaybeOnSubscribe<List<Game>>() {
            @Override
            public void subscribe(MaybeEmitter<List<Game>> e) throws Exception {
                final List<GameRomPath> gameRomPaths =
                        mGameRomPathDao.findAllByRomPathId(gameSystem.getRomPathID());
                final List<Long> gameIds;
                boolean deleteNotExists =
                        mSettings.getBoolean(SETTINGS_DELETE_NOT_EXISTS_ROM, false);

                if (deleteNotExists) {
                    final List<Game> games = mGameDao.findAllFlat(mGameSystem.getName());
                    for (Game game : games) {
                        if (!new File(game.getPath()).exists()
                                && (game.getStatus() == Game.STATUS_NORMAL
                                    || game.getStatus() == Game.STATUS_NEW_GAME)) {
                            mGameDao.delete(game);
                        }
                    }
                }

                scanFile(gameRomPaths);
                // 查询出游戏后进行处理
                if (!mResult.isEmpty()) {
                    gameIds = new ArrayList<>(mResult.size());

                    for (Game game : mResult) {
                        Game existsGame = mGameDao.findOne(game.getPath());

                        if (existsGame != null) {
                            if (!TextUtils.isEmpty(existsGame.getRepository())) {
                                continue;
                            }

                            boolean hasNewInfo = false;
                            if (game.getIcon() != null) {
                                existsGame.setIcon(game.getIcon());
                                hasNewInfo = true;
                            }
                            if (game.getDisplayNames() != null) {
                                existsGame.setDisplayNames(game.getDisplayNames());
                                hasNewInfo = true;
                            }
                            if (hasNewInfo) {
                                mGameDao.update(existsGame);
                            }

                            gameIds.add(existsGame.getId());
                        } else {
                            long id = mGameDao.save(game);
                            gameIds.add(id);
                        }
                    }

                    if (!gameIds.isEmpty()) {
                        for (long id : gameIds) {
                            GameSystemRef gameSystemRef = new GameSystemRef();
                            gameSystemRef.setGameId(id);
                            gameSystemRef.setSystem(mGameSystem.getName());

                            mGameSystemRefDao.insert(gameSystemRef);
                        }
                    }
                }

                e.onSuccess(mGameDao.findAllFlat(mGameSystem.getName()));
                e.onComplete();
            }
        });
    }

    private void scanFile(List<GameRomPath> gameRomPaths) {
        for (GameRomPath romPath : gameRomPaths) {
            File file = new File(romPath.getPath());

            if (!file.exists()) {
                continue;
            }

            if (!mGameNameFromFile.isInit()) {
                mGameNameFromFile.setMapFileName(mGameSystem.getPlatformId());
            }

            scanRecursion(file);

            romPath.setIsScanned(true);
            romPath.setScanDate(DateHelper.dateToString(new Date()));

            mGameRomPathDao.update(romPath);
        }

        mGameNameFromFile.clear();
    }

    private void scanRecursion(File file) {
        File files[] = file.listFiles(mFileFilter);
        Log.d(TAG, "Scanning dir: " + file.getPath());
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isFile()) {
                String path = f.getPath();
                String name = Utils.getFileName(path);
                String icon = null;
                String displayName = mGameNameFromFile.getDisplayName(name.toLowerCase(), path);

                if ("Neo Geo".equals(mGameSystem.getPlatform()) ||
                        "Arcade".equals(mGameSystem.getPlatform())) {
                    icon = Constants.MAME_ICON + File.separator + name + ".ico";

                    if (ARCADE_BIOS.contains(name)) {
                        continue;
                    }
                }

                Game game = new Game();
                game.setName(name);
                game.setDisplayNames(Converters.jsonToMap(displayName));
                game.setCardImageUrl(icon);
                game.setIcon(icon);
                game.setPath(path);
                game.setPlatformId(mGameSystem.getPlatformId());
                game.setRomPathId(mGameSystem.getRomPathID());
                game.setExt(Utils.getFileExt(path));

                mResult.add(game);
            } else {
                scanRecursion(f);
            }
        }
    }
}
