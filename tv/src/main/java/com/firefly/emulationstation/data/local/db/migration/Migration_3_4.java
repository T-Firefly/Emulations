package com.firefly.emulationstation.data.local.db.migration;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.firefly.emulationstation.data.repository.SystemsRepository;

import static com.firefly.emulationstation.commom.Constants.EXTERNAL_STORAGE;
import static com.firefly.emulationstation.commom.Constants.SETTINGS_DB_UPDATE_VERSION;

/**
 * Created by rany on 17-12-6.
 */

public class Migration_3_4 extends Migration {
    private SharedPreferences mSettings;

    /**
     * This constructor is for test.
     */
    public Migration_3_4() {
        this(null);
    }

    public Migration_3_4(SharedPreferences settings) {
        super(3, 4);

        mSettings = settings;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE Game ADD COLUMN romPathId TEXT");
        database.execSQL("ALTER TABLE Game ADD COLUMN ext TEXT");

        database.execSQL("CREATE TABLE `NewGameRomPath` (" +
                "`path` TEXT NOT NULL, " +
                "`romPathId` TEXT, " +
                "`isScanned` INTEGER NOT NULL, " +
                "`scanDate` TEXT, " +
                "PRIMARY KEY(`path`))");
        database.execSQL("INSERT INTO NewGameRomPath (path, romPathId, isScanned, scanDate)" +
                " SELECT path, system, isScanned, scanDate FROM GameRomPath");
        database.execSQL("DROP TABLE GameRomPath");
        database.execSQL("ALTER TABLE NewGameRomPath RENAME TO GameRomPath");

        database.execSQL("CREATE TABLE IF NOT EXISTS `DownloadInfo` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT, " +
                "`url` TEXT, " +
                "`size` INTEGER NOT NULL, " +
                "`progress` INTEGER NOT NULL, " +
                "`status` INTEGER NOT NULL, " +
                "`type` INTEGER NOT NULL, " +
                "`path` TEXT, " +
                "`repository` TEXT, " +
                "`version` TEXT, " +
                "`system` TEXT, " +
                "`createDate` INTEGER)");


        if (mSettings != null) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(SETTINGS_DB_UPDATE_VERSION, 1);
            editor.apply();
        }
    }
}
