package com.firefly.emulationstation;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.framework.FrameworkSQLiteOpenHelperFactory;
import android.arch.persistence.room.testing.MigrationTestHelper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.firefly.emulationstation.data.local.db.GameDatabase;
import com.firefly.emulationstation.data.local.db.migration.Migration_1_2;
import com.firefly.emulationstation.data.local.db.migration.Migration_2_3;
import com.firefly.emulationstation.data.local.db.migration.Migration_3_4;
import com.firefly.emulationstation.data.local.db.migration.Migration_4_5;
import com.firefly.emulationstation.data.local.db.migration.Migration_5_6;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by rany on 18-1-31.
 */

@RunWith(AndroidJUnit4.class)
public class MigrationTest {
    private static final String TEST_DB = "gamedb-test";

    @Rule
    public MigrationTestHelper helper;

    public MigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                GameDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate1To2() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 1);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db.execSQL("INSERT INTO \"Game\" VALUES('kov2','en:Knights of Valour 2 / Sangoku Senki 2 (ver. 107, 102, 100HK)::zh:三国战纪 2 (V107, 102, 100, 香港版)',NULL,'/sdcard/EmulationStation/roms/arcade/kov2.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/kov2.ico','/storage/emulated/0/EmulationStation/mame/icon/kov2.ico',NULL,NULL,0.0,'Arcade',0)");
        db.execSQL("INSERT INTO \"Game\" VALUES('kof97','en:The King of Fighters ''97 (NGM-2320)::zh:拳皇 ''97 (NGM-2320)',NULL,'/sdcard/EmulationStation/roms/arcade/kof97.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/kof97.ico','/storage/emulated/0/EmulationStation/mame/icon/kof97.ico',NULL,NULL,0.0,'Arcade',0)");
        db.execSQL("INSERT INTO \"Game\" VALUES('dino','en:Cadillacs and Dinosaurs::zh:恐龙新世纪 (930201 世界版)',NULL,'/sdcard/EmulationStation/roms/arcade/dino.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/dino.ico','/storage/emulated/0/EmulationStation/mame/icon/dino.ico',NULL,NULL,0.0,'Arcade',0)");
        db.execSQL("INSERT INTO \"Game\" VALUES('mslug','en:Metal Slug::zh:合金弹头 - 超级坦克-001',NULL,'/sdcard/EmulationStation/roms/arcade/mslug.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/mslug.ico','/storage/emulated/0/EmulationStation/mame/icon/mslug.ico',NULL,NULL,0.0,'Arcade',0);");

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, new Migration_1_2());

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }

    @Test
    public void migrate2To3() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 2);

        // db has schema version 1. insert some data using SQL queries.
        // You cannot use DAO classes because they expect the latest schema.
        db.execSQL("INSERT INTO \"Game\" VALUES('kov2','en:Knights of Valour 2 / Sangoku Senki 2 (ver. 107, 102, 100HK)::zh:三国战纪 2 (V107, 102, 100, 香港版)',NULL,'/sdcard/EmulationStation/roms/arcade/kov2.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/kov2.ico','/storage/emulated/0/EmulationStation/mame/icon/kov2.ico',NULL,NULL,0.0,'Arcade',0,0)");
        db.execSQL("INSERT INTO \"Game\" VALUES('kof97','en:The King of Fighters ''97 (NGM-2320)::zh:拳皇 ''97 (NGM-2320)',NULL,'/sdcard/EmulationStation/roms/arcade/kof97.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/kof97.ico','/storage/emulated/0/EmulationStation/mame/icon/kof97.ico',NULL,NULL,0.0,'Arcade',0,0)");
        db.execSQL("INSERT INTO \"Game\" VALUES('dino','en:Cadillacs and Dinosaurs::zh:恐龙新世纪 (930201 世界版)',NULL,'/sdcard/EmulationStation/roms/arcade/dino.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/dino.ico','/storage/emulated/0/EmulationStation/mame/icon/dino.ico',NULL,NULL,0.0,'Arcade',0,0)");
        db.execSQL("INSERT INTO \"Game\" VALUES('mslug','en:Metal Slug::zh:合金弹头 - 超级坦克-001',NULL,'/sdcard/EmulationStation/roms/arcade/mslug.zip',NULL,'/storage/emulated/0/EmulationStation/mame/icon/mslug.ico','/storage/emulated/0/EmulationStation/mame/icon/mslug.ico',NULL,NULL,0.0,'Arcade',0,0)");

        // Prepare for the next version.
        db.close();

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 3, true, new Migration_2_3());

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
    }

    @Test
    public void migrate3To4() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);


        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, new Migration_3_4());
    }

    @Test
    public void migrate4To5() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 4);


        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, new Migration_4_5());
    }

    @Test
    public void migrate5To6() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 5);

        db.execSQL("INSERT INTO \"Game\" VALUES('Nekketsu Koukou Dodgeball Bu - Soccer Hen (J)',NULL,NULL,'/storage/emulated/0/EmulationStation/roms/nes/Nekketsu Koukou Dodgeball Bu - Soccer Hen (J).nes',NULL,NULL,NULL,NULL,NULL,0.0,'nes','ee2a20f2ad3055c4bf04638dce9da6b5','.nes',0,0,NULL,0);");
        db.execSQL("INSERT INTO \"Game\" VALUES('Contra (J)',NULL,NULL,'/storage/2A0B-3FAC/EmulationStation/roms/nes/Contra (J).nes',NULL,NULL,NULL,NULL,NULL,0.0,'nes','ee2a20f2ad3055c4bf04638dce9da6b5','.nes',0,0,NULL,0);");
        db.execSQL("INSERT INTO \"Game\" VALUES('Jackal (U)',NULL,NULL,'/storage/emulated/0/EmulationStation/roms/nes/Jackal (U).nes',NULL,NULL,NULL,NULL,NULL,0.0,'nes','ee2a20f2ad3055c4bf04638dce9da6b5','.nes',0,0,NULL,0);");
        db.execSQL("INSERT INTO \"Game\" VALUES('Mighty Final Fight (J)',NULL,NULL,'/storage/emulated/0/EmulationStation/roms/nes/Mighty Final Fight (J).nes',NULL,NULL,NULL,NULL,NULL,0.0,'nes','ee2a20f2ad3055c4bf04638dce9da6b5','.nes',0,0,NULL,0);");
        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, new Migration_5_6());
    }
}