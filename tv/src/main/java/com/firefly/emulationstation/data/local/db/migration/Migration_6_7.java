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

public class Migration_6_7 extends Migration {

    public Migration_6_7() {
        super(6, 7);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("DROP TABLE Fanart");
    }
}
