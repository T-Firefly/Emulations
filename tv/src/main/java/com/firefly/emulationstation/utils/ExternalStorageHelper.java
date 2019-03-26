package com.firefly.emulationstation.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.firefly.emulationstation.BuildConfig;
import com.firefly.emulationstation.R;
import com.firefly.emulationstation.SplashActivity;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.local.SystemsSource;
import com.firefly.emulationstation.data.repository.SystemsRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by rany on 17-10-26.
 */

@Singleton
public class ExternalStorageHelper {
    private static final int BUFFER_SIZE = 1024*48;

    private Context mContext;

    @Inject
    SystemsRepository mSystemsRepository;

    @Inject
    public ExternalStorageHelper(Context context) {
        mContext = context;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public Observable<Integer> init(){
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                File esdir = new File(Constants.ES_DIR);
                File fm = new File(Constants.ES_DIR, ".init");
                AssetManager assetManager = mContext.getAssets();

                if (!isExternalStorageReadable() || !isExternalStorageWritable()) {
                    e.onError(new Throwable(mContext.getString(R.string.external_storage_cant_write)));
                    return;
                }

                boolean hasNew = false;
                if(fm.exists() && !(hasNew = checkNeedUpgradeRes(fm))) {
                    e.onNext(SplashActivity.MSG_FINISH);
                    return;
                }

                if (hasNew) {
                    e.onNext(SplashActivity.MSG_UPDATE_RESOURCES);
                    prepareUpgrade();
                } else {
                    e.onNext(SplashActivity.MSG_INITIALIZING);
                }

                copyFile(assetManager, Constants.ES_DIR_NAME);

                if (hasNew)
                    mergeUpgrade();

                if (esdir.exists() || esdir.mkdirs()) {
                    updateInitFile(fm);
                }
                e.onNext(SplashActivity.MSG_FINISH);
            }
        });
    }

    private void prepareUpgrade() throws IOException {
        File file = new File(Constants.ES_DIR, "systems.xml");
        File backupFile = new File(Constants.ES_DIR, "systems-backup.xml");

        copy(file, backupFile);
    }

    private void mergeUpgrade() throws Exception {
        File file = new File(Constants.ES_DIR, "systems.xml");
        File backupFile = new File(Constants.ES_DIR, "systems-backup.xml");

        List<GameSystem> newGameSystems = SystemsSource.fileToObj(file);
        List<GameSystem> oldGameSystems = SystemsSource.fileToObj(backupFile);

        for (GameSystem oldSystem : oldGameSystems) {
            for (GameSystem newSystem : newGameSystems) {
                if (oldSystem.getName().equals(newSystem.getName())) {
                    newSystem.setEnable(oldSystem.isEnable());
                }
            }
        }

        mSystemsRepository.saveGameSystems(newGameSystems, file);
        backupFile.delete();
    }

    /**
     *
     * @param file the File instance of .init file
     * @return true if need upgrade the resource file, or false
     * @throws IOException
     */
    private boolean checkNeedUpgradeRes(File file) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        String versionStr = null;
        int version = -1;

        if ((versionStr = fileReader.readLine()) != null) {
            version = Integer.valueOf(versionStr);
        }
        fileReader.close();
        return version == -1 || version < BuildConfig.VERSION_CODE;
    }

    /**
     * Update .init file
     * @param fm the File instance of .init file
     * @throws IOException
     */
    private void updateInitFile(File fm) throws IOException {
        if (fm.exists() || fm.createNewFile()) {
            FileWriter fileWriter = new FileWriter(fm, false);
            fileWriter.write(String.valueOf(BuildConfig.VERSION_CODE));
            fileWriter.flush();
            fileWriter.close();
        }
    }

    private void copyFile(AssetManager assetManager, String file) throws IOException {

        String[] files = assetManager.list(file);

        if (files == null) {
            return;
        }

        if (files.length > 0) {
            File newDir = new File(Constants.EXTERNAL_STORAGE, file);
            newDir.mkdirs();

            for (String f : files) {
                copyFile(assetManager, file + "/" + f);
            }
        } else {
            copyFile(assetManager, file, Constants.EXTERNAL_STORAGE + "/" + file);
        }
    }

    private void copyFile(AssetManager assetManager, String oldFile, String newFile)
            throws IOException {
        InputStream in = assetManager.open(oldFile);
        File outFile = new File(newFile);
        OutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }

        in.close();
        out.close();
    }

    /*
     * Copy external storage file
     */
    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    /**
     * Copy a directory to dest
     * @param src source
     * @param dest destination
     * @throws IOException
     */
    public static void copyDir(File src, File dest) throws IOException {
        if (src == null || dest == null || src.isFile() || dest.isFile()) {
            return;
        }

        dest.mkdirs();

        for (File file : src.listFiles()) {
            if (file.isDirectory()) {
                copyDir(file, new File(dest, file.getName()));
            } else {
                File destFile = new File(dest, file.getName());
                copy(file, destFile);
            }
        }
    }

    /**
     * Delete a file or a directory
     * @param file Which file will be deleted
     */
    public static void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }

        file.delete();
    }

    /**
     * Delete a file or a directory via a path
     * @param path Which path will be deleted
     */
    public static void delete(String path) {
        delete(new File(path));
    }
}
