package com.firefly.emulationstation.commom;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.SearchOrbView;
import android.support.v17.leanback.widget.TitleViewAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firefly.emulationstation.R;

/**
 * Created by rany on 17-11-3.
 */

public class ESTitleView extends FrameLayout implements TitleViewAdapter.Provider {
    private static final String TAG = ESTitleView.class.getSimpleName();

    private ImageView mBadgeView;
    private TextView mTitleView;
    private LinearLayout mSearchOrbView;

    private final TitleViewAdapter mTitleViewAdapter = new TitleViewAdapter() {

        @Override
        public View getSearchAffordanceView() {
            return mSearchOrbView;
        }

        @Override
        public void setTitle(CharSequence titleText) {
            ESTitleView.this.setTitle(titleText);
        }

        @Override
        public void setBadgeDrawable(Drawable drawable) {
            ESTitleView.this.setBadgeDrawable(drawable);
        }

        @Override
        public void setOnSearchClickedListener(OnClickListener listener) {
            mSearchOrbView.setOnClickListener(listener);
        }

        @Override
        public void updateComponentsVisibility(int flags) {
            int visibility = (flags & SEARCH_VIEW_VISIBLE) == SEARCH_VIEW_VISIBLE
                    ? View.VISIBLE : View.INVISIBLE;
            mSearchOrbView.setVisibility(visibility);
            mBadgeView.setVisibility(visibility);
        }

        @Override
        public void setSearchAffordanceColors(SearchOrbView.Colors colors) {
//            SearchOrbView searchOrbView = mSearchOrbView.findViewById(R.id.search_badge);
//            searchOrbView.setOrbColors(colors);
        }
    };

    public ESTitleView(Context context) {
        this(context, null);
    }

    public ESTitleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ESTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.es_title_view, this);

        mBadgeView = (ImageView) rootView.findViewById(R.id.title_badge);
        mTitleView = (TextView) rootView.findViewById(R.id.title_text);
        mSearchOrbView = rootView.findViewById(R.id.search_orb);
    }

    public void setTitle(CharSequence title) {
        if (title != null) {
            mTitleView.setText(title);
            mTitleView.setVisibility(View.VISIBLE);
        }
    }

    public void setBadgeDrawable(Drawable drawable) {
        if (drawable != null) {
            mTitleView.setVisibility(View.GONE);
            mBadgeView.setVisibility(View.VISIBLE);
            mBadgeView.setImageDrawable(drawable);
        }
    }

    @Override
    public TitleViewAdapter getTitleViewAdapter() {
        return mTitleViewAdapter;
    }

}
