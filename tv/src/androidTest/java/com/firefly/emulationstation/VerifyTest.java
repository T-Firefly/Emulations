package com.firefly.emulationstation;

import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.firefly.emulationstation.utils.ExternalStorageHelper;
import com.firefly.emulationstation.utils.NetworkHelper;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static junit.framework.Assert.assertEquals;

/**
 * Created by rany on 18-6-1.
 */

@RunWith(AndroidJUnit4.class)
public class VerifyTest {
    @Test
    public void fileExtTest() {
        String filename = "abcd.ef";
        assertEquals(filename.substring(0, filename.lastIndexOf('.')), "abcd");
        assertEquals(filename.substring(filename.lastIndexOf('.')), ".ef");
    }

    @Test
    public void getFileExtFromUrl() {
        String url = "http://download.freeroms.com/mame_roms/p/pgm.zip";
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).head().build();
        try {
            Response response = client.newCall(request).execute();
            System.err.println(response.headers().toString());
            String contentType = response.header("Content-Type");
            String mimeType = null;

            if (contentType != null) {
                mimeType = contentType.split(";")[0];
            }
            String fileName = URLUtil.guessFileName(url, null, mimeType);
            System.err.println(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void getCorrectFileToDeleteTest() {
//        String path = "/storage/emulated/0/EmulationStation/roms/gba/com.firefly/1214 - Oriental Blue - Ao no Tengai (J).gba";
//        String result = path;
        String path = "/storage/emulated/0/EmulationStation/roms/gba/com.firefly/1214 - Oriental Blue - Ao no Tengai (J)/1214 - Oriental Blue - Ao no Tengai (J).gba";
        String result = "/storage/emulated/0/EmulationStation/roms/gba/com.firefly/1214 - Oriental Blue - Ao no Tengai (J)";
        int index = path.indexOf("/com.firefly/");
        System.err.println(index);

        int stop = 3;
        for (; index < path.length(); ++index) {
            if (path.charAt(index) == '/') {
                --stop;
            }

            if (stop == 0) {
                break;
            }
        }
        System.err.println(path.substring(0, index));
        assertEquals(path.substring(0, index), result);
    }

    @Test
    public void postUrlTest() {
        String postUrl = "http://www.planetemu.net/php/roms/download.php#!post:id=3129114&download=T%C3%A9l%C3%A9charger";
        String url = "http://www.planetemu.net/php/roms/download.php";

        Assert.assertTrue(NetworkHelper.isPostUrl(postUrl));
        Assert.assertFalse(NetworkHelper.isPostUrl(url));

        assertEquals(NetworkHelper.getPostUrl(postUrl), url);

        RequestBody data = NetworkHelper.getPostData(postUrl);
        Assert.assertNotNull(data);

        Assert.assertNull(NetworkHelper.getPostData(url));
    }

    @Test
    public void postDownloadTest() {
        String url = "http://www.planetemu.net/php/roms/download.php#!post:id=3129114&download=T%C3%A9l%C3%A9charger";
        String ext = null;
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request.Builder builder = new Request.Builder();

        if (NetworkHelper.isPostUrl(url)) {
            builder.url(NetworkHelper.getPostUrl(url));
            builder.method("HEAD", NetworkHelper.getPostData(url));
        } else {
            builder.url(url);
            builder.head();
        }

        Request  request = builder.build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("=====", response.code() + " \n");
            Assert.assertTrue(response.isSuccessful());

            String contentType = response.header("Content-Type");
            String mimeType = null;

            if (contentType != null) {
                mimeType = contentType.split(";")[0];
            }
            Log.d("=====", mimeType + " 1");

            Log.d("====", response.header("Content-Disposition") + "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
