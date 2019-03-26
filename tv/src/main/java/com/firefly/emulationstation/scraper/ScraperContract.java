package com.firefly.emulationstation.scraper;

import com.firefly.emulationstation.commom.presenter.BasePresenter;
import com.firefly.emulationstation.commom.presenter.BaseView;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;

import java.util.List;

/**
 * Created by rany on 17-12-18.
 */

public interface ScraperContract {
    interface View extends BaseView<Presenter> {
        void showProgressBar(String gameName);
        void hideProgressBar();
        void showOptionsGameInfo(List<Game> games);
        void finish();
    }

    interface Presenter extends BasePresenter<View> {
        void nextGame();
        void initData(long[] ids, String[] systemNames, boolean autoSelect);
        void fetchGameInfoOptions();
        void saveGameInfo();
        Game getSelectedGameInfo();
        Game getSelectedGameInfo(int index);
        void setSelectedGameInfo(int index);
        boolean isAutoSelect();
        void clearGameInfo();
    }
}
