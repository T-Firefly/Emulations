package com.firefly.emulationstation.inputmap;

import android.app.Activity;
import android.os.Bundle;
import android.support.v17.leanback.app.GuidedStepFragment;

import com.firefly.emulationstation.R;

public class InputMapActivity extends Activity {

    public static final String PARAMS_DEVICE_IDS = "deviceIds";
    public static final String PARAMS_DEVICE_INDEX = "deviceIndex";
    public static final String PARAMS_PLAYER_INDEX = "playerIndex";

    private InputMapPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InputMapFragment fragment = InputMapFragment.newInstance(getIntent().getExtras());

        if (getIntent().hasExtra(PARAMS_PLAYER_INDEX)) {
            fragment.setPresenter(new KeyboardPresenter(fragment, this));
        } else {
            mPresenter = new GamepadPresenter(fragment, this);
        }

        GuidedStepFragment.addAsRoot(this, fragment, android.R.id.content);
    }

    @Override
    public void onBackPressed() {

    }
}
