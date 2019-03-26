package com.firefly.emulationstation.scraper;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.app.ProgressBarManager;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.GlideApp;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.utils.I18nHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * A simple {@link GuidedStepFragment} subclass.
 */
public class ScraperOptionsFragment extends GuidedStepFragment
        implements ScraperContract.View {
    private static final String TAG = ScraperOptionsFragment.class.getSimpleName();
    private static final int GUIDED_ACTION_CLEAR_ID = -1;

    private TextView mTitleView;
    private TextView mBreadcrumbView;
    private TextView mDescriptionView;
    private TextView mProgressTextView;
    private ImageView mIconView;

    @Inject
    ProgressBarManager mProgressBarManager;
    private ScraperContract.Presenter mPresenter;

    private final List<GuidedAction> mSubActions = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mSubActions.add(new GuidedAction.Builder(getActivity())
                .id(android.R.string.ok)
                .title(android.R.string.ok)
                .build());
        mSubActions.add(new GuidedAction.Builder(getActivity())
                .id(android.R.string.cancel)
                .title(android.R.string.cancel)
                .build());
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

        Log.d(TAG, "onPause");
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.guidedstep_background_view_root);

        View progressViewParent = LayoutInflater.from(getActivity())
                .inflate(R.layout.progressbar_with_text, viewGroup);

        mProgressTextView = (TextView)progressViewParent.findViewById(R.id.text);

        mProgressTextView.setText(R.string.loading);
        mProgressBarManager.setProgressBarView(progressViewParent.findViewById(R.id.progressBar));
        mProgressBarManager.show();

        mTitleView = getGuidanceStylist().getTitleView();
        mBreadcrumbView = getGuidanceStylist().getBreadcrumbView();
        mDescriptionView = getGuidanceStylist().getDescriptionView();
        mIconView = getGuidanceStylist().getIconView();

        mIconView.setVisibility(View.VISIBLE);
        mIconView.setAdjustViewBounds(true);
    }

    @Override
    public void onGuidedActionFocused(GuidedAction action) {
        super.onGuidedActionFocused(action);

        Game game = null;
        int index = (int) action.getId();

        if (index == GUIDED_ACTION_CLEAR_ID) {
            return;
        }

        try {
            game = mPresenter.getSelectedGameInfo(index);
        } catch (IndexOutOfBoundsException e) {
            return;
        }

        if (!action.hasSubActions() && game == mPresenter.getSelectedGameInfo()) {
            return;
        }

        mPresenter.setSelectedGameInfo(index);

        mTitleView.setText(game.getName());
        mBreadcrumbView.setText(GameSystem.getPlatformName(game.getPlatformId()));

        if (game.getDescription() != null) {
            mDescriptionView.setText(I18nHelper.getValueFromMap(game.getDescription()));
        } else {
            mDescriptionView.setText(R.string.no_description);
        }

        GlideApp.with(getActivity())
                .load(game.getCardImageUrl())
                .error(R.drawable.game)
                .fitCenter()
                .into(mIconView);
    }

    @Override
    public boolean onSubGuidedActionClicked(GuidedAction action) {
        if (action.getId() == android.R.string.ok) {
            mPresenter.saveGameInfo();
        }

        return super.onSubGuidedActionClicked(action);
    }

    @Override
    public void setPresenter(ScraperContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showProgressBar(String gameName) {
        if (getActivity() == null) {
            return;
        }
        mProgressTextView.setText(getString(R.string.scraper_for, gameName));

        mProgressBarManager.show();
    }

    @Override
    public void hideProgressBar() {
        mProgressBarManager.hide();
    }

    @Override
    public void showOptionsGameInfo(List<Game> games) {
        List<GuidedAction> guidedActions = new ArrayList<>();
        Context context = getActivity();

        if (context == null) {
            return;
        }

        for (int i = 0; i < games.size(); ++i) {
            Game game = games.get(i);
            GuidedAction guidedAction = new GuidedAction.Builder(context)
                    .id(i)
                    .title(game.getName())
                    .editable(false)
                    .description(game.getDeveloper())
                    .subActions(mSubActions)
                    .build();

            guidedActions.add(guidedAction);
        }

        GuidedAction guidedAction = new GuidedAction.Builder(context)
                .id(GUIDED_ACTION_CLEAR_ID)
                .title(R.string.clear_game_info)
                .editable(false)
                .build();

        guidedActions.add(guidedAction);

        setActions(guidedActions);
        onGuidedActionFocused(guidedActions.get(0));
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        super.onGuidedActionClicked(action);

        if (action.getId() == GUIDED_ACTION_CLEAR_ID) {
            mPresenter.clearGameInfo();
        }
    }

    @Override
    public void finish() {
        getActivity().setResult(Activity.RESULT_OK);
        finishGuidedStepFragments();
    }
}
