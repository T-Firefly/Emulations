package com.firefly.emulationstation.data.repository;

import com.firefly.emulationstation.data.bean.GamePlay;
import com.firefly.emulationstation.data.exceptions.RetroArchCanNotGetInfoException;
import com.firefly.emulationstation.data.local.CoreSource;
import com.firefly.emulationstation.data.remote.VersionRemoteSource;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class GamePlayRepository {
    private VersionRemoteSource mVersionRemoteSource;
    private CoreSource mCoreSource;

    @Inject
    GamePlayRepository(VersionRemoteSource versionRemoteSource,
                       CoreSource coreSource) {
        mVersionRemoteSource = versionRemoteSource;
        mCoreSource = coreSource;
    }

    public Observable<List<GamePlay>> getGamePlays() {
        return Observable.create(new ObservableOnSubscribe<List<GamePlay>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GamePlay>> e) throws Exception {
                try {
                    e.onNext(getGamePlaysDirectly());
                } catch (RetroArchCanNotGetInfoException e1) {
                    e.onError(e1);
                }
                e.onComplete();
            }
        });
    }

    public List<GamePlay> getGamePlaysDirectly() throws RetroArchCanNotGetInfoException {
        List<GamePlay> gamePlays = mCoreSource.getCores();
        GamePlay retroarch = mVersionRemoteSource
                .getRetroArch(gamePlays.get(0).getUrl());

        if (retroarch == null) {
            throw new RetroArchCanNotGetInfoException();
        }
        retroarch.setName(gamePlays.get(0).getName());
        gamePlays.set(0, retroarch);

        return gamePlays;
    }
}
