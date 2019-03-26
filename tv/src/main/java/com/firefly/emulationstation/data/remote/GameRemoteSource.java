package com.firefly.emulationstation.data.remote;

import com.firefly.emulationstation.data.bean.Game;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by rany on 17-12-19.
 */

public interface GameRemoteSource {
    Observable<Game> getGameDetail(final Game game, final String platform);
    Observable<List<Game>> getGameDetailOptions(final Game game, final String platform);
}
