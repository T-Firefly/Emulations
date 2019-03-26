package com.firefly.emulationstation.commom;

import android.os.Environment;

/**
 * Created by rany on 17-10-26.
 */

public class Constants {
    public static final String EXTERNAL_STORAGE =
            Environment.getExternalStorageDirectory().getPath();
    public static final String ES_DIR_NAME = "EmulationStation";
    public static final String ES_DIR = EXTERNAL_STORAGE + "/" + ES_DIR_NAME;
    public static final String MAME_ICON = ES_DIR + "/mame/icon";
    public static final String RETRO_CONFIG_FILE = ES_DIR + "/config/retroarch.cfg";
    public static final String ES_MAP_DIR = ES_DIR + "/map";
    public static final String CORES_DIR = ES_DIR + "/cores";
    public static final String CRASH_DIR = ES_DIR + "/crash";
    public static final String CACHE_DIR = ES_DIR + "/cache";
    public static final String REPO_DIR = ES_DIR + "/repository";

    public static final String UPDATE_APK = CACHE_DIR + "/emulations.apk";
    public static final String RETROARCH_APK = CACHE_DIR + "/RetroArch.apk";
    public static final String RETROARCH_PACKAGE_NAME = "com.retroarch";

    public static final int SUPPORT_MAX_PLAYER = 4;
    public static final int DEFAULT_DOWNLOAD_THREAD = 3;

    // Settings preference key
    public static final String SETTINGS_CHECK_GAMEPAD = "prefs_check_gamepad";
    public static final String SETTINGS_SHOW_STAR_GAMES_HEADER = "prefs_show_star";
    public static final String SETTINGS_DELETE_NOT_EXISTS_ROM = "prefs_delete_not_exists_rom";
    public static final String SETTINGS_MAX_DOWNLOAD_THREAD = "prefs_max_download_thread";
    public static final String SETTINGS_SHOW_RECOMMENDED_GAME = "prefs_show_recommended";
    public static final String SETTINGS_USE_THE_GAMES_DB_NEW_API = "prefs_use_new_api";

    public static final String SETTINGS_DB_UPDATE_VERSION = "db_update_version";
    public static final String SETTINGS_FIRST_START = "first_start";
    public static final String SETTINGS_SHOW_ONBOARDING = "prefs_show_onboarding";
    public static final String SETTINGS_IGNORE_VERSION = "ignore_version";

    public static final String ROM_PATH_DEVICE_PREFIX= "${device}";
    
    public static final String ROM_PATH_INTERNAL_PREFIX = "${internal}";

    public static final String BROADCAST_GAME_SYSTEM_CHANGED = "broadcast_game_system_changed";
    public static final String BROADCAST_DOWNLOAD_COMPLETED = "DownloadService.COMPLETED";
    public static final String BROADCAST_DOWNLOAD_ERROR = "DownloadService.ERROR";

    public static final String POST_FLAG = "#!post:";
    public static final String THE_GAMES_DB_APPKEY = "THE GAMES DB APPKEY";
}
