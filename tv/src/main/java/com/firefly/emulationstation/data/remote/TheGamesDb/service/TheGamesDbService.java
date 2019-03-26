package com.firefly.emulationstation.data.remote.TheGamesDb.service;

import com.firefly.emulationstation.data.remote.TheGamesDb.bean.GamesResponse;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.ImagesResponse;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.PlatformsResponse;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TheGamesDbService {
    /**
     * This API will return a list or a single game entry based the provided parameter(s).
     * This API call expects `name`, and `apikey`, while `include` and `fields` are optional.
     * apikey will append at request
     *
     * @param name the game name, in English
     * @param platforms int array of platform
     * @param include valid options: `boxart,platform`.
     * @param fields valid options: `id,game_title,players,release_date,developers,
     *               publisher(deprecated),publishers,genres,overview,last_updated,rating,platform,
     *               coop,youtube,os,processor,ram,hdd,video,sound,alternates(format changed),`
     * @return Observable
     */
    @GET("/Games/ByGameName")
    Observable<GamesResponse> getGameDetail(@Query("name") String name,
                                            @Query("filter[platform]") int[] platforms,
                                            @Query("include") String[] include,
                                            @Query("fields") String[] fields);

    /**
     * This API will return the platform console list
     *
     * @return object of Call<PlatformsResponse>
     */
    @GET("/Platforms")
    Call<PlatformsResponse> getPlatforms();

    /**
     * This API will return a list or a single graphics entry based the provided parameter(s).
     * This API call expects `id`, and `apikey` param, while `filter` param is optional.
     * apikey will append at request
     *
     * @param ids long array of game id
     * @param filter valid options: `banner,fanart,boxart`.
     * @return object of Call<ImagesResponse>
     */
    @GET("/Platforms/images")
    Call<ImagesResponse> getImages(@Query("id") long[] ids,
                                   @Query("filter[type]") String[] filter);
}
