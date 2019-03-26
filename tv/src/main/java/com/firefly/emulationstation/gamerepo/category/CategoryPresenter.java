package com.firefly.emulationstation.gamerepo.category;

import android.app.Fragment;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.SystemsRepository;
import com.firefly.emulationstation.di.ActivityScoped;
import com.firefly.emulationstation.gamerepo.data.bean.Category;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rany on 18-4-23.
 */

@ActivityScoped
public class CategoryPresenter implements CategoryContract.Presenter {
    public final static int GAME_NOT_INSTALL = 0;
    public final static int GAME_INSTALLED = 1;

    private CategoryContract.View mView;

    private SystemsRepository mSystemsRepository;
    private OnListFragmentInteractionListener mListener;
    private GameSystem mSelectGameSystem = null;

    @Inject
    CategoryPresenter(SystemsRepository systemsRepository) {
        mSystemsRepository = systemsRepository;
    }

    @Override
    public void subscribe(CategoryContract.View view) {
        mView = view;

        mSystemsRepository.getEnableGameSystem(false)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<GameSystem>>() {
                    @Override
                    public void accept(List<GameSystem> gameSystems) throws Exception {
                        List<Category> categories = new ArrayList<>();
                        for (GameSystem system : gameSystems) {
                            categories.add(new Category(system.getName(), system));
                        }
                        categories.add(new Category(((Fragment)mView).getString(R.string.repo_manager)));

                        mView.showCategory(categories);

                        if (mSelectGameSystem != null) {
                            mView.setSelectedSystem(mSelectGameSystem);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void unsubscribe() {
        mView = null;
    }

    @Override
    public void onCategorySelected(Category category) {
        mListener.onListFragmentInteraction(category);
    }

    @Override
    public void onInstallStatusChange(int status) {
        mListener.onInstallStatusChange(status);
    }

    @Override
    public void setSelectedSystem(GameSystem gameSystem) {
        mSelectGameSystem = gameSystem;
    }

    @Override
    public void setOnListFragmentInteractionListener(OnListFragmentInteractionListener listener) {
        mListener = listener;
    }
}
