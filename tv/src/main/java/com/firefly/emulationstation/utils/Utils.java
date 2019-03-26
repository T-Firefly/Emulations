/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.firefly.emulationstation.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.Dependency;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.exceptions.RetroArchCoreNotExistsException;
import com.firefly.emulationstation.services.downloader.DownloadService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * A collection of utility methods, all static.
 */
public class Utils {

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }

    /**
     * Returns the screen/display size
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    /**
     * Shows a (long) toast
     */
    public static void showToast(final Context context, final String msg) {
        if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Shows a (long) toast.
     */
    public static void showToast(Context context, int resourceId) {
            showToast(context, context.getString(resourceId));
    }

    public static int convertDpToPixel(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    /**
     * Formats time in milliseconds to hh:mm:ss string format.
     */
    public static String formatMillis(int millis) {
        String result = "";
        int hr = millis / 3600000;
        millis %= 3600000;
        int min = millis / 60000;
        millis %= 60000;
        int sec = millis / 1000;
        if (hr > 0) {
            result += hr + ":";
        }
        if (min >= 0) {
            if (min > 9) {
                result += min + ":";
            } else {
                result += "0" + min + ":";
            }
        }
        if (sec > 9) {
            result += sec;
        } else {
            result += "0" + sec;
        }
        return result;
    }

    public static String getFileName(String path) {
        String name = path;
        int start = path.lastIndexOf(File.separator) + 1;
        int end = path.lastIndexOf(".");

        if (start != -1 && end != -1) {
            name = path.substring(start, end);
        }

        return name;
    }

    public static String getFileExt(String path) {
        int index = path.lastIndexOf(".");
        if (index == -1) {
            return "";
        }

        return path.substring(index).toLowerCase();
    }

    public static void startGame(Context context, GameSystem gameSystem, Game game)
            throws ActivityNotFoundException, RetroArchCoreNotExistsException {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.retroarch",
                "com.retroarch.browser.mainmenu.MainMenuActivity"));
        String config = gameSystem.getEmulator().getConfig();
        String corePath = gameSystem.getEmulator().getCorePath();
        if (!new File(corePath).exists()) {
            throw new RetroArchCoreNotExistsException(corePath);
        }

        intent.putExtra("ROM", game.getPath());
        intent.putExtra("LIBRETRO", corePath);
        intent.putExtra("CONFIGFILE", config == null ? Constants.RETRO_CONFIG_FILE : config);
        intent.putExtra("DATADIR", context.getFilesDir().getPath() + "/data/com.retroarch");
        intent.putExtra("APK", "/system/app/RetroArch");
//        intent.putExtra("QUITFOCUS", true);

        context.startActivity(intent);
    }

    public static String getGameDisplayName(String displayName, String code) {
        if (displayName != null && !displayName.isEmpty()) {
            if (!displayName.contains(code + ":")) {
                return null;
            }

            String[] names = displayName.split("::");
            for (String n : names) {
                if (n.startsWith(code+":") && n.length() > 3) {
                    return n.substring(code.length()+1);
                }
            }
        }


        return null;
    }

    public static String joinString(char delimiter, String[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for (String str : array) {
            builder.append(str);
            builder.append(delimiter);
        }

        return builder.deleteCharAt(builder.length()-1).toString();
    }

    public static void installApk(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri fileUri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true) ;
            intent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static void uninstallApp(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.fromParts("package", packageName,null));
        context.startActivity(intent);
    }

    public static boolean isRetroArchInstalled(Context context) {
        return isAppInstall(context, "com.retroarch");
    }

    public static boolean isAppInstall(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignore) {
        }

        return false;
    }

    public static void downloadGameDependencies(List<Dependency> dependencies,
                                                DownloadService downloadService,
                                                String romPath) {
        if (dependencies != null) {
            for (Dependency dep : dependencies) {
                String path;
                if (TextUtils.isEmpty(dep.getPath())) {
                    path = romPath.substring(0,
                            romPath.lastIndexOf('/') + 1);

                    path += dep.getName();
                } else {
                    path = Constants.ES_DIR + '/' + dep.getPath();
                }

                if (!new File(path).exists()) {
                    DownloadInfo info = new DownloadInfo(dep.getName(),
                            dep.getUrl(), DownloadInfo.TYPE_ROM_DEPENDENCY, path, "");

                    try {
                        downloadService.download(info);
                    } catch (Throwable ignore) {
                    }
                }
            }
        }
    }

    public static String findRom(File file, Set<String> extensions) throws IOException {
        String romPath = null;

        if (!file.exists()) {
            return null;
        }

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                romPath = findRom(f, extensions);
                if (romPath != null) {
                    return romPath;
                }

                continue;
            }

            for (String ext : extensions) {
                if (f.getPath().endsWith(ext)
                        || f.getPath().endsWith(ext.toUpperCase())) {
                    return f.getCanonicalPath();
                }
            }
        }

        return null;
    }
}
