package com.firefly.emulationstation.utils;

import android.util.Log;

import com.firefly.emulationstation.gamerepo.data.bean.Plat;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by rany on 18-3-24.
 */

public class ZipHelper {
    public static void decompress(File zip, File out) throws IOException {
        try (InputStream inputStream = new FileInputStream(zip)) {
            decompress(inputStream, out);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Decompress a zip file to a directory
     * @param zip Zip file input stream to decompress
     * @param out The directory to save decompress file
     * @throws IOException
     */
    public static void decompress(InputStream zip, File out)
            throws IOException {
        ZipInputStream zipStream = new ZipInputStream(zip);
        ZipEntry zipEntry;
        byte[] buffer = new byte[1024];

        if (out.isFile()) {
            throw new IOException("Parameter out can't be a file, it should be a dir.");
        }

        while ((zipEntry = zipStream.getNextEntry()) != null) {
            String filename = zipEntry.getName();

            File file = new File(out, filename);
            if (zipEntry.isDirectory()) {
                file.mkdirs();
                continue;
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            int length = 0;
            while ((length = zipStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            zipStream.closeEntry();
        }
    }

    /**
     * Check the file is valid or not.
     * @param file the package extract path file
     * @param type json or other
     * @return true if file is a valid package or false
     */
    public static boolean isValidRepoPackage(File file, String type) {
        String ext = "."+type;
        try {
            File repoFile = new File(file, "repo" + ext);
            File platsFile = new File(file, "platforms");

            if (!repoFile.exists() && !platsFile.isFile()) {
                return false;
            } else if (!platsFile.exists() && !platsFile.isDirectory()) {
                return false;
            }

            Repo repo = Repo.fromFile(repoFile);

            List<Plat> platList = repo.getPlatforms();
            if (platList == null || platList.size() <= 0) {
                return false;
            }

            for (Plat plat : platList) {
                File platFile = new File(platsFile, plat.getPlatformId() + ext);
                if (!platFile.exists()) {
                    return false;
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getWrapWithDir(File zipFile) {
        try {
            ZipInputStream zipStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zipStream.getNextEntry();

            if (zipEntry == null) {
                return null;
            }

            String filename = zipEntry.getName();
            int index = filename.indexOf('/');
            if (index == -1) {
                return null;
            }

            String suffix = filename.substring(0, index);

            while ((zipEntry = zipStream.getNextEntry()) != null) {
                if (!zipEntry.getName().startsWith(suffix)) {
                    return null;
                }
            }

            return suffix;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
