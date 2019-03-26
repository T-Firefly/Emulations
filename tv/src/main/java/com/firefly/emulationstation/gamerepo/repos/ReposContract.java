package com.firefly.emulationstation.gamerepo.repos;

import com.firefly.emulationstation.commom.presenter.BasePresenter;
import com.firefly.emulationstation.commom.presenter.BaseView;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;

import java.util.List;

/**
 * Created by rany on 18-4-25.
 */

public interface ReposContract {
    interface View extends BaseView<Presenter> {
        void showRepos(List<Repo> repos);
        void showMessage(String msg);
        void updateRepo(int position, Repo repo);
        void showAddRepoTypeSelector();
        void showRepoActions(Repo repo);
        void showDeleteConfirm(Repo repo);
        void showProgress(String msg, boolean show);
    }

    interface Presenter extends BasePresenter<View> {
        void addRepo(String path);
        void onAddRepoButtonClick(android.view.View view);
        void deleteRepo(Repo repo, boolean confirm);
        void updateRepo(Repo repo, boolean confirm);
    }
}
