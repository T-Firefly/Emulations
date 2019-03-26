package com.firefly.emulationstation.services.downloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.DownloadInfo;

import javax.inject.Inject;

/**
 * Created by rany on 18-3-21.
 */

public class NotificationHelper {
    public final static String CHANNEL_ID = "com.firefly.emulation_station";

    private Context mContext;

    @Inject
    public NotificationHelper(Context context) {
        mContext = context;
    }

    public void showNotification(DownloadInfo info) {
        double size = info.getSize() == 0 ? 1 : info.getSize();
        int progress = (int) ((info.getProgress() / size) * 100);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_mode_edit)
                        .setContentTitle(info.getPath().substring(info.getPath().lastIndexOf('/') + 1));

        switch (info.getStatus()) {
            case DownloadInfo.STATUS_AUTO_START:
                builder.setContentText(mContext.getString(R.string.downloading));
                break;
            case DownloadInfo.STATUS_COMPLETED:
                builder.setContentText(mContext.getString(R.string.download_completed));
                break;
            case DownloadInfo.STATUS_DOWNLOADING:
                builder.setProgress(100, progress, false);
                break;
            case DownloadInfo.STATUS_ERROR:
                builder.setContentText(mContext.getString(R.string.download_error));
                break;
            case DownloadInfo.STATUS_PENDING:
                builder.setContentText(mContext.getString(R.string.downloading));
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = mContext.getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Register the channel with the system
            NotificationManager notificationManager =
                    (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(info.getId(), builder.build());
    }
}
