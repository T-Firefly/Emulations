package com.firefly.emulationstation.commom.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v17.leanback.widget.FocusHighlight;
import android.support.v17.leanback.widget.VerticalGridPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.view.ViewGroup;

import com.firefly.emulationstation.R;

/**
 * Created by rany on 18-5-10.
 */

public class GameGridPresenter extends VerticalGridPresenter {

    public GameGridPresenter() {
        super(FocusHighlight.ZOOM_FACTOR_MEDIUM, false);

        init();
    }

    private void init() {
        setShadowEnabled(false);
    }

    @Override
    protected ViewHolder createGridViewHolder(ViewGroup parent) {
        ViewHolder viewHolder = super.createGridViewHolder(parent);
        VerticalGridView mGridView = viewHolder.getGridView();
        Context context = mGridView.getContext();
        Resources res = context.getResources();

        mGridView.setHorizontalSpacing((int) res.getDimension(R.dimen.game_grid_item_h_space));
        mGridView.setVerticalSpacing((int) res.getDimension(R.dimen.game_grid_item_v_space));

        return viewHolder;
    }
}
