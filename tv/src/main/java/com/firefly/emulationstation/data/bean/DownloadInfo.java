package com.firefly.emulationstation.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by rany on 18-3-21.
 */

@Entity
public class DownloadInfo implements Serializable {
    @IntDef({TYPE_ROM, TYPE_EXTENSION, TYPE_CORE, TYPE_APK, TYPE_REPO, TYPE_ROM_DEPENDENCY})
    public @interface TYPE {}
    public static final int TYPE_ROM = 0;
    public static final int TYPE_EXTENSION = 1;
    public static final int TYPE_CORE = 2;
    public static final int TYPE_APK = 3;
    public static final int TYPE_REPO = 4;
    public static final int TYPE_ROM_DEPENDENCY = 5;

    @IntDef({STATUS_COMPLETED, STATUS_ERROR, STATUS_DOWNLOADING,
            STATUS_AUTO_START, STATUS_PENDING, STATUS_PAUSE, STATUS_STOP})
    public @interface STATUS {}
    public static final int STATUS_COMPLETED = 0;
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_AUTO_START = 3;
    public static final int STATUS_PENDING = 4;
    public static final int STATUS_PAUSE = 5;
    public static final int STATUS_STOP = 6;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String url;
    private long size;
    private long progress;
    private int status;
    private int type;
    private String path;
    /**
     * For resume download
     */
    private String lastModified;
    private String version;
    private Date createDate;

    @Ignore
    private Object ref;

    public DownloadInfo(
            @NonNull String name,
            @NonNull String url,
            int type,
            String path,
            String version) {
        this.name = name;
        this.url = url;
        this.type = type;
        this.version = version;
        this.path = path;

        this.status = STATUS_PENDING;
        this.progress = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getProgress() {
        return progress;
    }

    public int getShowProgress() {
        double size = getSize() == 0 ? 1.0 : getSize();
        return (int) ((getProgress() / size) * 100);
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    @STATUS
    public int getStatus() {
        return status;
    }

    public void setStatus(@STATUS int status) {
        this.status = status;
    }

    public boolean isDownloading() {
        return status == STATUS_DOWNLOADING;
    }

    public boolean isCompleted() {
        return status == STATUS_COMPLETED;
    }

    @TYPE
    public int getType() {
        return type;
    }

    public void setType(@TYPE int type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getCreateDate() {
        if (createDate == null) {
            createDate = new Date();
        }
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Object getRef() {
        return ref;
    }

    public void setRef(Object ref) {
        this.ref = ref;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DownloadInfo)) {
            return false;
        }

        DownloadInfo info = (DownloadInfo) obj;

        if (info.getId() == id && id != 0) {
            return true;
        }

        if (!info.getUrl().equals(url)) {
            return false;
        } else if (info.getVersion() != null
                && !info.getVersion().equals(version)) {
            return false;
        } else if (info.getType() != type) {
            return false;
        }

        return true;
    }
}
