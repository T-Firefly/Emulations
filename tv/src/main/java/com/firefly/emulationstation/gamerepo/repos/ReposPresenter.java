package com.firefly.emulationstation.gamerepo.repos;

import android.content.Context;
import android.view.View;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.exceptions.RepositoryAlreadyNewestException;
import com.firefly.emulationstation.data.exceptions.RepositoryDownloadErrorException;
import com.firefly.emulationstation.data.exceptions.RepositoryExistsException;
import com.firefly.emulationstation.data.exceptions.RepositoryInvalidException;
import com.firefly.emulationstation.data.exceptions.RepositoryNotSupportException;
import com.firefly.emulationstation.di.ActivityScoped;
import com.firefly.emulationstation.gamerepo.data.GameRepoRepository;
import com.firefly.emulationstation.gamerepo.data.bean.Repo;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rany on 18-4-25.
 */

@ActivityScoped
public class ReposPresenter implements ReposContract.Presenter,
        RepoAdapter.OnItemClickListener {
    private ReposContract.View mView;

    private Context mContext;

    private GameRepoRepository mGameRepoRepository;
    private Disposable mAddDisposable;

    @Inject
    ReposPresenter(GameRepoRepository gameRepoRepository, Context context) {
        mGameRepoRepository = gameRepoRepository;
        mContext = context;
    }

    @Override
    public void subscribe(ReposContract.View view) {
        mView = view;

        refreshRepos();
    }

    @Override
    public void unsubscribe() {
        if (mAddDisposable != null && !mAddDisposable.isDisposed()) {
            mAddDisposable.dispose();
        }
    }

    @Override
    public void addRepo(String path) {
        mView.showProgress(mContext.getString(R.string.adding_repository), true);

        addOrUpdateRepo(path, true);
    }

    @Override
    public void onAddRepoButtonClick(View view) {
        mView.showAddRepoTypeSelector();
    }

    @Override
    public void deleteRepo(Repo repo, boolean confirm) {
        if (confirm) {
            mGameRepoRepository.deleteRepo(repo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Set<Repo>>() {
                        @Override
                        public void accept(Set<Repo> repos) throws Exception {
                            mView.showRepos(new ArrayList<>(repos));
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        } else {
            mView.showDeleteConfirm(repo);
        }
    }

    @Override
    public void updateRepo(Repo repo, boolean confirm) {
        mView.showProgress(mContext.getString(R.string.updating_repository), true);

        addOrUpdateRepo(repo.getUrl(), false);
    }

    @Override
    public void onClick(Repo repo) {
        mView.showRepoActions(repo);
    }

    private void refreshRepos() {
        mGameRepoRepository.repos()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Set<Repo>>() {
                    @Override
                    public void accept(Set<Repo> repos) throws Exception {
                        mView.showRepos(new ArrayList<>(repos));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    private void addOrUpdateRepo(String path, boolean isNew) {
        mAddDisposable = mGameRepoRepository.addOrUpdateRepo(path, isNew)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Repo>() {
                    @Override
                    public void accept(Repo repo) {
                        refreshRepos();

                        mAddDisposable.dispose();
                        mView.showProgress(null, false);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        mView.showProgress(null, false);
                        String msg = throwable.getMessage();
                        if (throwable instanceof RepositoryExistsException) {
                            msg = mContext.getString(R.string.repo_already_exists);
                        } else if (throwable instanceof RepositoryDownloadErrorException) {
                            msg = mContext.getString(R.string.repo_download_error);
                        } else if (throwable instanceof RepositoryInvalidException) {
                            msg = mContext.getString(R.string.repo_invalid);
                        } else if (throwable instanceof FileNotFoundException) {
                            msg = mContext.getString(R.string.file_not_found);
                        } else if (throwable instanceof RepositoryNotSupportException) {
                            msg = mContext.getString(R.string.repo_not_support);
                        } else if (throwable instanceof RepositoryAlreadyNewestException) {
                            msg = mContext.getString(R.string.repo_is_newest);
                        }

                        mView.showMessage(msg);
                        throwable.printStackTrace();
                    }
                });
    }
}
