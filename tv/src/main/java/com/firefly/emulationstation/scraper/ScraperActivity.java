package com.firefly.emulationstation.scraper;

import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.view.KeyEvent;

import javax.inject.Inject;

import dagger.android.DaggerActivity;

public class ScraperActivity extends DaggerActivity {
    public static final String ARG_GAME_SYSTEMS = "system";
    public static final String ARG_GAMES = "games";
    public static final String ARG_AUTO_SELECT = "auto_select";

    @Inject
    ScraperPresenter mScraperPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] systemNames = getIntent().getStringArrayExtra(ARG_GAME_SYSTEMS);
        long[] gameNames = getIntent().getLongArrayExtra(ARG_GAMES);
        boolean autoSelect = getIntent().getBooleanExtra(ARG_AUTO_SELECT, true);

        mScraperPresenter.initData(gameNames, systemNames, autoSelect);
        createFragment();
    }

    @Override
    public void onBackPressed() {
        if (GuidedStepFragment.getCurrentGuidedStepFragment(getFragmentManager())
                instanceof ScraperOptionsFragment) {
            // The user 'bought' the product. When he presses 'Back' the Wizard will be closed and
            // he will not be send back to 'Processing Payment...'-Screen.
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private void createFragment() {
        ScraperOptionsFragment fragment = new ScraperOptionsFragment();
        fragment.setPresenter(mScraperPresenter);

        GuidedStepFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mScraperPresenter.isAutoSelect() && keyCode != KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
