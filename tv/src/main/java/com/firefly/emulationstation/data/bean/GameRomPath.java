package com.firefly.emulationstation.data.bean;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Created by rany on 17-10-31.
 */

@Entity
public class GameRomPath {
    @PrimaryKey
    @NonNull
    String path;
    String romPathId;
    boolean isScanned;
    String scanDate;

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public String getRomPathId() {
        return romPathId;
    }

    public void setRomPathId(String romPathId) {
        this.romPathId = romPathId;
    }

    public boolean getIsScanned() {
        return isScanned;
    }

    public void setIsScanned(boolean isScanned) {
        this.isScanned = isScanned;
    }

    public String getScanDate() {
        return scanDate;
    }

    public void setScanDate(String scanDate) {
        this.scanDate = scanDate;
    }
}
