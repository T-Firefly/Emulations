package com.firefly.emulationstation.data.local.db.migration;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;

import static com.firefly.emulationstation.commom.Constants.SETTINGS_DB_UPDATE_VERSION;

/**
 * Created by rany on 17-12-6.
 */

public class Migration_5_6 extends Migration {
    private SharedPreferences mSettings;

    public Migration_5_6() {
        this(null);
    }
    /**
     * This constructor is for test.
     */
    public Migration_5_6(SharedPreferences settings) {
        super(5, 6);

        mSettings = settings;
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `newGame` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`displayNames` TEXT, " +
                "`description` TEXT, " +
                "`path` TEXT NOT NULL, " +
                "`backgroundImageUrl` TEXT, " +
                "`cardImageUrl` TEXT," +
                "`icon` TEXT, " +
                "`videoUrl` TEXT, " +
                "`developer` TEXT, " +
                "`rating` REAL NOT NULL, " +
                "`platformId` TEXT, " +
                "`romPathId` TEXT, " +
                "`ext` TEXT, " +
                "`isScraped` INTEGER NOT NULL, " +
                "`isStar` INTEGER NOT NULL, " +
                "`repository` TEXT, " +
                "`status` INTEGER NOT NULL, " +
                "`downloadId` INTEGER NOT NULL DEFAULT 0, " +
                "`url` TEXT, " +
                "`version` TEXT, " +
                "`dependencies` TEXT)");
        database.execSQL("INSERT INTO `newGame`(`name`,`displayNames`,`description`,`path`," +
                "`backgroundImageUrl`,`cardImageUrl`,`icon`,`videoUrl`,`developer`,`rating`," +
                "`platformId`,`romPathId`,`ext`,`isScraped`,`isStar`,`repository`,`status`) " +
                "SELECT `name`,`displayNames`,`description`,`path`," +
                "`backgroundImageUrl`,`cardImageUrl`,`icon`,`videoUrl`,`developer`,`rating`," +
                "`system`,`romPathId`,`ext`,`isScraped`,`isStar`,`repository`,`status` FROM game");

        database.execSQL("DROP TABLE Game");
        database.execSQL("ALTER TABLE newGame RENAME TO Game");

        // new table
        database.execSQL("CREATE TABLE IF NOT EXISTS `GameSystemRef` (" +
                "`gameId` INTEGER NOT NULL, " +
                "`system` TEXT NOT NULL, " +
                "`isStar` INTEGER NOT NULL, " +
                "PRIMARY KEY(`gameId`, `system`), " +
                "FOREIGN KEY(`gameId`) REFERENCES `Game`(`id`) " +
                "ON UPDATE NO ACTION ON DELETE CASCADE )");

        database.execSQL("CREATE TABLE IF NOT EXISTS `NewDownloadInfo` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT, `url` TEXT, " +
                "`size` INTEGER NOT NULL, " +
                "`progress` INTEGER NOT NULL, " +
                "`status` INTEGER NOT NULL, " +
                "`type` INTEGER NOT NULL, " +
                "`path` TEXT, " +
                "`lastModified` TEXT, " +
                "`version` TEXT, " +
                "`createDate` INTEGER)");

        Cursor cursor = database.query("select DISTINCT tbl_name from sqlite_master where tbl_name = 'DownloadInfo'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                database.execSQL("INSERT INTO `NewDownloadInfo` (`id`,`name`,`size`,`progress`,`status`," +
                        "`type`,`path`,`version`,`createDate`) SELECT `id`,`name`,`size`,`progress`," +
                        "`status`,`type`,`path`,`version`,`createDate` FROM `DownloadInfo`");
                database.execSQL("DROP TABLE DownloadInfo");
            }
            cursor.close();
        }

        database.execSQL("ALTER TABLE NewDownloadInfo RENAME TO DownloadInfo");

        if (mSettings != null) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(SETTINGS_DB_UPDATE_VERSION, 2);
            editor.apply();
        }
    }
}
