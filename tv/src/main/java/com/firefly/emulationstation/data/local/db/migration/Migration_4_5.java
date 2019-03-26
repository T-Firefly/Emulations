package com.firefly.emulationstation.data.local.db.migration;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.migration.Migration;
import android.support.annotation.NonNull;

/**
 * Created by rany on 18-4-26.
 */

public class Migration_4_5 extends Migration {

    /**
     * This constructor is for test.
     */
    public Migration_4_5() {
        super(4, 5);
    }

    @Override
    public void migrate(@NonNull SupportSQLiteDatabase database) {
        database.execSQL("ALTER TABLE Game ADD COLUMN repository TEXT");
        database.execSQL("ALTER TABLE Game ADD COLUMN status INTEGER DEFAULT 0 NOT NULL");
    }
}