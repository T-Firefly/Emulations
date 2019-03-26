package com.firefly.emulationstation;

/**
 * Created by rany on 18-2-5.
 */

import android.content.SharedPreferences;

import com.firefly.emulationstation.utils.DateHelper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.inject.Inject;

import static com.firefly.emulationstation.commom.Constants.CRASH_DIR;

public class AppCrashHandler implements Thread.UncaughtExceptionHandler{
    private static final String TAG = "AppCrashHandler";
    private static AppCrashHandler mCrashHandler;

    @Inject
    SharedPreferences mSettings;

    private AppCrashHandler(){

    }

    public static AppCrashHandler getInstace(){
        if(mCrashHandler == null)
            mCrashHandler = new AppCrashHandler();
        return mCrashHandler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();

        File crashFile = new File(CRASH_DIR, "crash-"+DateHelper.dateToString(new Date()) + ".log");
        crashFile.getParentFile().mkdirs();

        try {
            if (crashFile.createNewFile() && crashFile.canWrite()) {
                PrintWriter printWriter = new PrintWriter(crashFile);
                e.printStackTrace(printWriter);

                printWriter.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
