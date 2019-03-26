package com.firefly.emulationstation.guide;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * A simple {@link Fragment} subclass.
 */
public class GuideFragment extends OnboardingFragment {
    private static final long ANIMATION_DURATION = 500;

    private static int[] mPageTitles = new int[] {
            R.string.welcome,
            R.string.notice_download_retroarch
    };
    private static int[] mPageDescriptions = new int[] {
            R.string.welcome_desc,
            R.string.notice_download_retroarch_desc
    };
    private static int[] mPageContentViews = new int[] {
            R.layout.welcome_layout,
            R.layout.notice_download_retroarch_layout
    };

    @Inject
    SharedPreferences mSettings;
    private ViewGroup mContentViewParent;
    private Animator mContentAnimator;

    private Intent mData = new Intent();

    public GuideFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setLogoResourceId(R.drawable.logo);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getPageCount() {
        return mPageTitles.length;
    }

    @Override
    protected CharSequence getPageTitle(int pageIndex) {
        if (pageIndex == 0) {
            return "";
        }

        return getString(mPageTitles[pageIndex]);
    }

    @Override
    protected CharSequence getPageDescription(int pageIndex) {
        if (pageIndex == 0) {
            return "";
        }

        return getString(mPageDescriptions[pageIndex]);
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        View bgView = new View(getActivity());
        bgView.setBackgroundColor(getResources().getColor(R.color.guide_bg));
        return bgView;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        mContentViewParent = new FrameLayout(getActivity());

        return mContentViewParent;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Override
    protected void onPageChanged(final int newPage, int previousPage) {
        if (mContentAnimator != null) {
            mContentAnimator.end();
        }

        ArrayList<Animator> animators = new ArrayList<>();
        Animator fadeOut = createFadeOutAnimator(mContentViewParent);

        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                int layout = mPageContentViews[newPage];
                createView(layout);
            }
        });

        animators.add(fadeOut);
        animators.add(createFadeInAnimator(mContentViewParent));
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(animators);
        set.start();
        mContentAnimator = set;
    }

    @Nullable
    @Override
    protected Animator onCreateEnterAnimation() {
        createView(mPageContentViews[0]);

        mContentAnimator = createFadeInAnimator(mContentViewParent);
        return mContentAnimator;
    }

    @Override
    protected void onFinishFragment() {
        super.onFinishFragment();

        SharedPreferences.Editor editor = mSettings.edit();

        editor.putBoolean(Constants.SETTINGS_SHOW_ONBOARDING, false);
        editor.apply();

        getActivity().setResult(Activity.RESULT_OK, mData);
        getActivity().finish();
    }

    private void createView(int resId) {
        if (resId != -1) {
            View view = LayoutInflater
                    .from(getActivity())
                    .inflate(resId, mContentViewParent, false);

            mContentViewParent.removeAllViews();
            mContentViewParent.addView(view);

            switch (resId) {
                case R.layout.welcome_layout:
                    break;
                case R.layout.notice_download_retroarch_layout:
                    CheckBox checkBox = view.findViewById(R.id.install_retroarch_checkbox);

                    if (checkBox.isChecked()) {
                        mData.putExtra(GuideActivity.INSTALL_RETROARCH, true);
                    } else {
                        mData.putExtra(GuideActivity.INSTALL_RETROARCH, false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Animator createFadeInAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f)
                .setDuration(ANIMATION_DURATION);
    }

    private Animator createFadeOutAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f)
                .setDuration(ANIMATION_DURATION);
    }
}
