package com.firefly.emulationstation.gamerepo.repogames;

import com.firefly.emulationstation.commom.fragment.MenuDialog;
import com.firefly.emulationstation.commom.presenter.BasePresenter;
import com.firefly.emulationstation.commom.presenter.BaseView;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.gamerepo.data.bean.Category;

import java.util.List;

/**
 * Created by rany on 18-4-23.
 */

public interface RepoGamesContract {
    interface View extends BaseView<Presenter> {
        void showGames(List<Game> games);
        void setLoading(boolean show);
        void showError(String msg);
        void updateDownloadProgress(DownloadInfo downloadInfo);
        void showInstallNoticeView(Game game);
        void showRemoveNoticeView(Game game);
        void showGameExistsView(Game game);
        void showDownloadManagerMenu(List<MenuDialog.MenuItem> menuItems);
        void showEmptyView(boolean show);
    }

    interface Presenter extends BasePresenter<View> {
        void onCategorySelectChanged(Category category);
        void setInstallStatus(int status);
        void downloadGame(final Game game, boolean override);
        void onGameSelected(Game game, boolean ok);
        void onMenuSelected(MenuDialog.MenuItem menuItem);
    }
}
