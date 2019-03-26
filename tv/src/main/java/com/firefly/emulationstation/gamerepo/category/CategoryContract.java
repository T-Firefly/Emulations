package com.firefly.emulationstation.gamerepo.category;

import com.firefly.emulationstation.commom.presenter.BasePresenter;
import com.firefly.emulationstation.commom.presenter.BaseView;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.gamerepo.data.bean.Category;

import java.util.List;

/**
 * Created by rany on 18-4-23.
 */

public interface CategoryContract {
    interface View extends BaseView<Presenter> {
        void showCategory(List<Category> categories);
        void setSelectedSystem(GameSystem gameSystem);
    }

    interface Presenter extends BasePresenter<View> {
        void onCategorySelected(Category category);
        void onInstallStatusChange(int status);
        void setSelectedSystem(GameSystem gameSystem);

        void setOnListFragmentInteractionListener(OnListFragmentInteractionListener listener);

        interface OnListFragmentInteractionListener {
            void onListFragmentInteraction(Category item);
            void onInstallStatusChange(int installStatus);
        }
    }
}
