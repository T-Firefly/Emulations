package com.firefly.emulationstation.data.local.db.migration;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.firefly.emulationstation.commom.Constants.EXTERNAL_STORAGE;

/**
 * Created by rany on 17-12-6.
 */

public class Migration_2_3 extends Migration {

    public Migration_2_3() {
        super(2, 3);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("CREATE TABLE NewGame (" +
                "name TEXT NOT NULL, " +
                "displayNames TEXT, " +
                "description TEXT, " +
                "path TEXT NOT NULL, " +
                "backgroundImageUrl TEXT, " +
                "cardImageUrl TEXT, " +
                "icon TEXT, " +
                "videoUrl TEXT, " +
                "developer TEXT, " +
                "rating REAL NOT NULL, " +
                "system TEXT, " +
                "isScraped INTEGER NOT NULL, " +
                "isStar INTEGER NOT NULL, " +
                "PRIMARY KEY(path))");
        database.execSQL("INSERT INTO NewGame SELECT * FROM Game");
        database.execSQL("DROP TABLE Game");
        database.execSQL("ALTER TABLE NewGame RENAME TO Game");

        database.execSQL("UPDATE Game SET path = replace(path, '/sdcard', " +
                "'" + EXTERNAL_STORAGE + "')");
        database.execSQL("UPDATE GameRomPath SET path = replace(path, '/sdcard', " +
                "'" + EXTERNAL_STORAGE + "'), isScanned = 0");
    }
}
