package com.firefly.emulationstation.data.repository;

import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.local.db.DownloadInfoDao;
import com.firefly.emulationstation.data.local.db.GameDao;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rany on 18-3-24.
 */

@Singleton
public class DownloadRepository {
    private DownloadInfoDao mDownloadInfoDao;
    private GameDao mGameDao;

    @Inject
    DownloadRepository(DownloadInfoDao downloadInfoDao, GameDao gameDao) {
        mDownloadInfoDao = downloadInfoDao;
        mGameDao = gameDao;
    }

    public DownloadInfo findOne(int id) {
        DownloadInfo info = mDownloadInfoDao.findOne(id);
        if (info != null) {
            Game game = mGameDao.findByDownloadId(info.getId());

            info.setRef(game);
        }
        return info;
    }

    public DownloadInfo findOneByPath(String path) {
        return mDownloadInfoDao.findOneByPath(path);
    }

    public DownloadInfo findOneByUrl(String url) {
        return mDownloadInfoDao.findOneByUrl(url);
    }

    public long save(DownloadInfo info) {
        if (info.getId() > 0) {
            mDownloadInfoDao.update(info);
            return info.getId();
        }

        return mDownloadInfoDao.save(info);
    }

    public int update(List<DownloadInfo> infos) {
        return mDownloadInfoDao.update(infos);
    }

    public Maybe<List<DownloadInfo>> findWithGameByStatus(int... status) {
        return mDownloadInfoDao.findByStatus(status, DownloadInfo.TYPE_ROM)
                .toFlowable()
                .doOnNext(new Consumer<List<DownloadInfo>>() {
                    @Override
                    public void accept(List<DownloadInfo> list) throws Exception {
                        for (DownloadInfo downloadInfo : list) {
                            Game game = mGameDao.findByDownloadId(downloadInfo.getId());
                            downloadInfo.setRef(game);
                        }
                    }
                })
                .firstElement();
    }

    public void updateDownloadingStatusToPause() {
        Observable.create(
                new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        mDownloadInfoDao.updateAllStatus(DownloadInfo.STATUS_DOWNLOADING, DownloadInfo.STATUS_PAUSE);
                        mDownloadInfoDao.updateAllStatus(DownloadInfo.STATUS_PENDING, DownloadInfo.STATUS_PAUSE);
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
