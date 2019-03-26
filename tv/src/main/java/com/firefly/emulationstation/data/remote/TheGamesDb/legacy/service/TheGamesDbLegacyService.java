package com.firefly.emulationstation.data.remote.TheGamesDb.legacy.service;

import com.firefly.emulationstation.data.remote.TheGamesDb.legacy.bean.TheGamesDbGames;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by rany on 17-10-27.
 */

public interface TheGamesDbLegacyService {
    @GET("GetGame.php")
    Observable<TheGamesDbGames> getGameDetail(@Query("name") String name, @Query("platform") String platform);
}
