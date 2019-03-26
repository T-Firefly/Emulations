package com.firefly.emulationstation.commom.presenter;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v17.leanback.graphics.ColorOverlayDimmer;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.view.View;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.ImageCardView;

/**
 * Created by rany on 18-5-23.
 */

public class CardListRowPresenter extends ListRowPresenter {
    private ColorOverlayDimmer mColorDimmer;
    private int mTitleColor = -1;

    @Override
    public boolean isUsingDefaultListSelectEffect() {
        return false;
    }

    @Override
    protected void applySelectLevelToChild(ViewHolder rowViewHolder, View childView) {
        if (childView instanceof ImageCardView
                && Build.VERSION_CODES.M <= Build.VERSION.SDK_INT) {
            ImageCardView imageCardView = (ImageCardView) childView;
            View view = (View) imageCardView.getCardView().getParent();
            if (mColorDimmer == null) {
                mColorDimmer = ColorOverlayDimmer.createDefault(view.getContext());
            }
            mColorDimmer.setActiveLevel(rowViewHolder.getSelectLevel());
            int color = mColorDimmer.getPaint().getColor();

            if (mTitleColor == -1) {
                mTitleColor = view.getContext().getColor(R.color.image_card_view_title);
            }

            mTitleColor &= 0x00FFFFFF;
            mTitleColor |= (0xFF000000 - (color & 0xFF000000));

            view.setForeground(new ColorDrawable(color));
            imageCardView.setTitleColor(mTitleColor);
        } else {
            super.applySelectLevelToChild(rowViewHolder, childView);
        }
    }
}
