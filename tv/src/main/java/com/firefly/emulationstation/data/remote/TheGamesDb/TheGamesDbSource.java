package com.firefly.emulationstation.data.remote.TheGamesDb;

import android.content.Context;
import android.content.SharedPreferences;

import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.remote.GameRemoteSource;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.BaseUrl;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.Boxart;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.GamesResponse;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.Image;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.ImagesResponse;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.Platform;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.PlatformsResponse;
import com.firefly.emulationstation.data.remote.TheGamesDb.bean.RemoteGame;
import com.firefly.emulationstation.data.remote.TheGamesDb.service.TheGamesDbService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by rany on 17-10-30.
 */

@Singleton
public class TheGamesDbSource implements GameRemoteSource {
    private static final String PLATFORMS_SAVE_FILE = "platforms.data";
    private static final long VALID_TIME = 3*24*60*60*1000;

    private TheGamesDbService mTheGamesDbService;

    private String mGameName;
    private String mPlatform;
    private Map<String, Integer> mPlatforms = null;
    private Context mContext;
    private SharedPreferences mSettings;

    @Inject
    TheGamesDbSource(TheGamesDbService theGamesDbService,
                     Context context,
                     SharedPreferences settings) {
        mTheGamesDbService = theGamesDbService;
        mContext = context;
        mSettings = settings;
    }

    @Override
    public Observable<Game> getGameDetail(final Game game, final String platform) {
       return getGameDetails(game, platform)
                .flatMap(new Function<GamesResponse, ObservableSource<Game>>() {
                    @Override
                    public ObservableSource<Game> apply(GamesResponse gamesResponse)
                            throws Exception {
                        if (gamesResponse != null && gamesResponse.getData().getCount() > 0) {
                            RemoteGame tGame = gamesResponse.getData().getGames().get(0);
                            game.merge(tGame.toGame());

                            getBoxart(gamesResponse, tGame.getId(), game);
                            ImagesResponse imagesResponse = getFantarts(gamesResponse, game, platform);
                            getFantart(imagesResponse, tGame.getId(), game);

                            return Observable.just(game);
                        }

                        return Observable.error(new Throwable());
                    }
                });

    }

    @Override
    public Observable<List<Game>> getGameDetailOptions(final Game game, final String platform) {
        return getGameDetails(game, platform)
                .flatMap(new Function<GamesResponse, ObservableSource<List<Game>>>() {

                    @Override
                    public ObservableSource<List<Game>> apply(GamesResponse gamesResponse)
                            throws Exception {
                        List<Game> games = new ArrayList<>();

                        if (gamesResponse != null && gamesResponse.getData().getCount() > 0) {
                            games = new ArrayList<>((int) gamesResponse.getData().getCount());
                            List<RemoteGame> remoteGames = gamesResponse.getData().getGames();
                            ImagesResponse imagesResponse = getFantarts(gamesResponse, game, platform);

                            for (RemoteGame remoteGame : remoteGames) {
                                Game g = remoteGame.toGame();
                                getBoxart(gamesResponse, remoteGame.getId(), g);
                                getFantart(imagesResponse, remoteGame.getId(), g);

                                games.add(g);
                            }
                        }

                        return Observable.just(games);
                    }
                });

    }

    private String getDisplayName(Game game) {
        String name = game.getDisplayName("en");
        if (name == null) {
            name = game.getDisplayName("default");
        }
        if (name == null) {
            name = game.getName();
        }

        return name;
    }

    private Observable<GamesResponse> getGameDetails(final Game game, final String platform) {
        if (mPlatforms == null) {
            initPlatforms();
        }

        int[] platforms = new int[] {mPlatforms.get(platform)};
        final String name = getDisplayName(game);
        Observable<GamesResponse> remote = mTheGamesDbService
                .getGameDetail(name, platforms, new String[] {"boxart"},
                        new String[] {"overview"})
                .doOnNext(new Consumer<GamesResponse>() {
                    @Override
                    public void accept(GamesResponse gamesResponse) throws Exception {
                        writeFileObject(name + "-" + platform + ".data",
                                gamesResponse);
                        markDate(name + "-" + platform + ".date");
                    }
                });

        return Observable.concat(getFromCache(game, platform), remote)
                .firstElement()
                .toObservable();
    }

    private Observable<GamesResponse> getFromCache(final Game game, final String platform) {
        return Observable.create(new ObservableOnSubscribe<GamesResponse>() {
            @Override
            public void subscribe(ObservableEmitter<GamesResponse> e) {
                if (!isExpired(getDisplayName(game) + "-" + platform + ".date")) {
                    try {
                        GamesResponse gamesResponse =
                                (GamesResponse) readFileObject(
                                        getDisplayName(game) + "-" + platform + ".data");
                        e.onNext(gamesResponse);
                    } catch (Exception ignore) {
                    }
                }

                e.onComplete();
            }
        });
    }

    private ImagesResponse getFantarts(GamesResponse gamesResponse,
                                       Game game,
                                       String platform) throws IOException {
        ImagesResponse imagesResponse = null;
        String name = getDisplayName(game);
        String file = name + "-" + platform + "-images.data";
        String dateFile = name + "-" + platform + ".date";

        try {
            if (!isExpired(dateFile)) {
                imagesResponse = (ImagesResponse) readFileObject(file);
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            long[] gameIds = new long[(int) gamesResponse.getData().getCount()];
            int i = 0;
            for (RemoteGame remoteGame : gamesResponse.getData().getGames()) {
                gameIds[i++] = remoteGame.getId();
            }

            Call<ImagesResponse> responseCall = mTheGamesDbService
                    .getImages(gameIds, new String[]{"fanart"});

            imagesResponse = responseCall.execute().body();

            writeFileObject(file, imagesResponse);
            markDate(dateFile);
        }

        return imagesResponse;
    }

    private void getFantart(ImagesResponse imagesResponse,
                            long theGamesDbGameId,
                            Game game) {

        Image[] fanarts = null;
        BaseUrl baseUrl = null;
        if (imagesResponse != null && imagesResponse.getData() != null
                && imagesResponse.getData().getImages() != null) {
            fanarts = imagesResponse.getData().getImages().get(theGamesDbGameId);
            baseUrl = imagesResponse.getData().getBaseUrl();
        }

        if (fanarts != null && fanarts.length > 0) {
            Image image = fanarts[0];
            game.setBackgroundImageUrl(baseUrl.getOriginal() + image.getFilename());
        }
    }

    private void getBoxart(GamesResponse gamesResponse,
                           long theGameDbGameId,
                           Game game) {
        Image[] images = null;
        BaseUrl baseUrl = null;
        if (gamesResponse.getInclude() != null
                && gamesResponse.getInclude().getBoxart() != null) {
            Boxart boxart = gamesResponse.getInclude().getBoxart();

            if (boxart.getData() != null) {
                images = boxart.getData().get(theGameDbGameId);
            }
            baseUrl = boxart.getBaseUrl();
        }

        if (images != null) {
            for (Image image : images) {
                if ("boxart".equals(image.getType())
                        && "front".equals(image.getSide())) {
                    game.setCardImageUrl(baseUrl.getThumb()+image.getFilename());
                    break;
                }
            }
        }
    }

    private void initPlatforms() {
        try {
            mPlatforms = (Map<String, Integer>) readFileObject(PLATFORMS_SAVE_FILE);
        } catch (Exception e) {
            try {
                mPlatforms = new HashMap<>();
                Response<PlatformsResponse> response =
                        mTheGamesDbService.getPlatforms().execute();
                PlatformsResponse platformsResponse = response.body();

                if (platformsResponse != null && platformsResponse.getCode() == 200
                        && platformsResponse.getData() != null) {
                    Map<Integer, Platform> platforms = platformsResponse.getData().getPlatforms();

                    for (Map.Entry<Integer, Platform> entry : platforms.entrySet()) {
                        Platform platform = entry.getValue();
                        mPlatforms.put(platform.getName(), platform.getId());
                    }

                    writeFileObject(PLATFORMS_SAVE_FILE, mPlatforms);
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private Object readFileObject(String file) throws IOException, ClassNotFoundException {
        Object result;
        FileInputStream fis = mContext.openFileInput(file);
        ObjectInputStream is = new ObjectInputStream(fis);
        result = is.readObject();
        is.close();
        fis.close();

        return result;
    }

    private void writeFileObject(String file, Object object) {
        try {
            FileOutputStream fos = mContext.openFileOutput(file, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(object);
            os.close();
            fos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isExpired(String file) {
        FileInputStream fis = null;
        try {
            fis = mContext.openFileInput(file);
            DataInputStream dataInputStream = new DataInputStream(fis);
            long time = dataInputStream.readLong();
            dataInputStream.close();
            fis.close();

            return (new Date().getTime() - time > VALID_TIME);
        } catch (IOException ignore) {
        }

        return true;
    }

    private void markDate(String file) {
        FileOutputStream fos = null;
        try {
            fos = mContext.openFileOutput(file, Context.MODE_PRIVATE);
            DataOutputStream dataOutputStream = new DataOutputStream(fos);
            dataOutputStream.writeLong(new Date().getTime());

            dataOutputStream.close();
            fos.close();
        } catch (IOException ignore) {
        }
    }
}
