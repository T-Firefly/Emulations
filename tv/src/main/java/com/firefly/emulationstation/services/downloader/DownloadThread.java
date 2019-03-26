package com.firefly.emulationstation.services.downloader;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.utils.NetworkHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by rany on 18-3-21.
 */

public class DownloadThread implements Runnable {
    public static final String TAG = DownloadThread.class.getSimpleName();

    final private DownloadInfo mInfo;
    private OkHttpClient mClient;
    private File mDestination;
    private File mTmpFile;
    private Call mCall;

    private boolean mStop = false;
    private long mContentCount;

    private ProgressResponseBody.ProgressListener mProgressListener =
            new ProgressResponseBody.ProgressListener() {
                @Override
                public void update(long bytesRead, long contentLength, boolean done) {

                    synchronized (mInfo) {
                        if (done ||
                                (bytesRead == contentLength && contentLength != 0)) {
                            mInfo.setStatus(DownloadInfo.STATUS_COMPLETED);
                            mTmpFile.renameTo(mDestination);
                        }

                        mInfo.setProgress(mContentCount + bytesRead);
                    }
                }
            };

    public DownloadThread(DownloadInfo info) {
        this.mInfo = info;

        mClient = getProgressClient();
        mContentCount = mInfo.getProgress() < 0 ? 0 : mInfo.getProgress();
    }

    @Override
    public void run() {
        mStop = false;
        download();
    }

    private Call newCall() {
        String url = mInfo.getUrl();
        Request.Builder builder = new Request.Builder()
                .header("RANGE", "bytes=" + mContentCount + "-");

        if (NetworkHelper.isPostUrl(url)) {
            builder.url(NetworkHelper.getPostUrl(url));
            builder.post(NetworkHelper.getPostData(url));
        } else {
            builder.url(url);
        }

        if (!TextUtils.isEmpty(mInfo.getLastModified())) {
            builder.header("If-Range", mInfo.getLastModified());
        }

        return mClient.newCall(builder.build());
    }

    private OkHttpClient getProgressClient() {
        Interceptor interceptor = new Interceptor() {
            @Override
            public Response intercept(@NonNull Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), mProgressListener))
                        .build();
            }
        };

        return new OkHttpClient.Builder()
                .followSslRedirects(true)
                .connectTimeout(30, TimeUnit.SECONDS)
                .addNetworkInterceptor(interceptor)
                .build();
    }

    private void download() {
        mCall = newCall();
        try {
            Response response = mCall.execute();

            if (mInfo.getProgress() <= 0 && !redefineSavePath(response)) {
                mInfo.setStatus(DownloadInfo.STATUS_ERROR);
                return;
            }

            save(response, mContentCount);
        } catch (IOException e) {
            e.printStackTrace();
            mInfo.setStatus(DownloadInfo.STATUS_ERROR);
        }
    }

    public void pause() {
        mStop = true;

        if(mCall !=null){
            mCall.cancel();
        }
    }

    private void save(Response response, long startsPoint) {
        ResponseBody body = response.body();
        assert body != null;
        InputStream in = body.byteStream();
        FileChannel channelOut = null;
        RandomAccessFile randomAccessFile = null;

        mInfo.setSize(mContentCount + body.contentLength());
        mInfo.setLastModified(response.header("Last-Modified"));

        mDestination = new File(mInfo.getPath());
        mTmpFile = new File(mInfo.getPath() + ".part");

        try {
            randomAccessFile = new RandomAccessFile(mTmpFile, "rwd");
            channelOut = randomAccessFile.getChannel();

            MappedByteBuffer mappedBuffer =
                    channelOut.map(FileChannel.MapMode.READ_WRITE,
                            startsPoint,
                            body.contentLength());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                mappedBuffer.put(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (!mStop) {
                mInfo.setStatus(DownloadInfo.STATUS_ERROR);
            }
        }finally {
            try {
                in.close();
                if (channelOut != null) {
                    channelOut.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the correct download file path and file extension base on http response,
     * and check the response is valid or not.
     *
     * @param response Okhttp response object response from request.
     * @return true if this response is valid or false
     */
    private boolean redefineSavePath(Response response) {
        String ext = null;

        if (!response.isSuccessful()) {
            return false;
        }

        String fileName = null;
        String disposition = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(disposition)) {
            Pattern regex = Pattern.compile("(?<=filename=\").*?(?=\")");
            Matcher regexMatcher = regex.matcher(disposition);
            if (regexMatcher.find()) {
                fileName = regexMatcher.group();
            }
        }

        if (fileName == null) {
            String contentType = response.header("Content-Type");
            String mimeType = null;

            if (contentType != null) {
                mimeType = contentType.split(";")[0];
            }

            fileName = URLUtil.guessFileName(mInfo.getUrl(), null, mimeType);
        }

        if (fileName.endsWith(".html")
                || fileName.endsWith("htm")) {
            return false;
        } else if (!fileName.endsWith(".bin")) {
            ext = fileName.substring(fileName.lastIndexOf('.'));
        }

        String path = mInfo.getPath();
        if (ext != null) {
            path = path.substring(0, path.lastIndexOf('.')) + ext;
            mInfo.setPath(path);
        }

        File out = new File(path);
        out.getParentFile().mkdirs();

        return true;
    }
}
