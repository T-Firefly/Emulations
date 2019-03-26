package com.firefly.emulationstation.gamerepo.category;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.di.ActivityScoped;
import com.firefly.emulationstation.gamerepo.data.bean.Category;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static com.firefly.emulationstation.gamerepo.category.CategoryPresenter.GAME_INSTALLED;
import static com.firefly.emulationstation.gamerepo.category.CategoryPresenter.GAME_NOT_INSTALL;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the
 * {@link CategoryContract.Presenter.OnListFragmentInteractionListener} interface.
 */
@ActivityScoped
public class CategoryFragment extends Fragment implements CategoryContract.View {
    private RecyclerView mListView;
    private CategoryAdapter mCategoryAdapter;

    @Inject
    CategoryContract.Presenter mPresenter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CategoryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);
        mListView = view.findViewById(R.id.list);

        RadioGroup radioGroup = view.findViewById(R.id.install_status);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int result;

                switch (i) {
                    case R.id.not_install_rb:
                        result = GAME_NOT_INSTALL;
                        break;
                    case R.id.installed_rb:
                        result = GAME_INSTALLED;
                        break;
                    default:
                        result = GAME_NOT_INSTALL;
                        break;
                }

                mPresenter.onInstallStatusChange(result);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.subscribe(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mPresenter.unsubscribe();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void setPresenter(CategoryContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showCategory(List<Category> categories) {
        if (mCategoryAdapter == null) {
            mCategoryAdapter = new CategoryAdapter(categories, mPresenter);
            mListView.setAdapter(mCategoryAdapter);
        } else {
            mCategoryAdapter.setData(categories);
        }
    }

    @Override
    public void setSelectedSystem(GameSystem gameSystem) {
        List<Category> categories = mCategoryAdapter.getCategories();

        if (categories == null) {
            return;
        }

        for (Category category : categories) {
            if (category.gameSystem != null
                    && category.gameSystem.equals(gameSystem)) {
                mCategoryAdapter.setSelected(categories.indexOf(category));
                break;
            }
        }
    }
}
