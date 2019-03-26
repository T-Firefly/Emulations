package com.firefly.emulationstation.data.repository;

import com.firefly.emulationstation.data.bean.Version;
import com.firefly.emulationstation.data.remote.VersionRemoteSource;

import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class VersionRepository {
    private VersionRemoteSource mVersionRemoteSource;

    @Inject
    VersionRepository(VersionRemoteSource versionRemoteSource) {
        mVersionRemoteSource = versionRemoteSource;
    }

    public Version getVersionDirectly() throws IOException {
        return getVersionFromRemote(false);
    }

    public Observable<Version> getVersion() {
        return getVersion(false);
    }

    public Observable<Version> getVersion(final boolean refresh) {
        return Observable.create(new ObservableOnSubscribe<Version>() {
            @Override
            public void subscribe(ObservableEmitter<Version> e) throws Exception {
                Version version = getVersionFromRemote(refresh);
                e.onNext(version);
                e.onComplete();
            }
        });
    }

    private Version getVersionFromRemote(boolean refresh) throws IOException {
        return mVersionRemoteSource.getNewVersion(refresh);
    }
}
