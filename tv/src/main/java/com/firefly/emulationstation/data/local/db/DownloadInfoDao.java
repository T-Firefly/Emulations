package com.firefly.emulationstation.data.local.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.firefly.emulationstation.data.bean.DownloadInfo;

import java.util.List;

import io.reactivex.Maybe;

/**
 * Created by rany on 18-3-21.
 */

@Dao
public interface DownloadInfoDao {
    @Insert
    long save(DownloadInfo info);

    /**
     * Find DownloadInfo record by id.
     * @param id Download info id.
     * @return Matched DownloadInfo
     */
    @Query("SELECT * FROM DownloadInfo WHERE id = :id")
    DownloadInfo findOne(int id);
    @Query("SELECT * FROM DownloadInfo WHERE path = :path ORDER BY id DESC")
    DownloadInfo findOneByPath(String path);
    @Query("SELECT * FROM DownloadInfo WHERE url = :url ORDER BY id DESC LIMIT 1")
    DownloadInfo findOneByUrl(String url);

    /**
     * Find download info by status and type
     * @param status array of DownloadInfo.STATUS
     * @param type one of DownloadInfo.TYPE
     * @return Match downloadInfo list
     */
    @Query("SELECT * FROM DownloadInfo WHERE status IN (:status) AND type = :type")
    Maybe<List<DownloadInfo>> findByStatus(int[] status, int type);
    @Update
    int update(DownloadInfo info);
    @Update
    int update(List<DownloadInfo> list);

    @Query("UPDATE DownloadInfo SET status = :newStatus WHERE status = :preStatus")
    void updateAllStatus(int preStatus, int newStatus);

    /**
     * Delete a DownloadInfo by id
     * @param id id of DownloadInfo which want to delete.
     * @return 1 if deleted or 0
     */
    @Query("DELETE FROM DownloadInfo WHERE id = :id")
    int deleteById(int id);
}
