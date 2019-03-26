package com.firefly.emulationstation.guide;

import android.os.Bundle;
import android.view.KeyEvent;

import com.firefly.emulationstation.R;

import dagger.android.AndroidInjection;
import dagger.android.DaggerActivity;

public class GuideActivity extends DaggerActivity {
    public static final String INSTALL_RETROARCH = "install_retroarch";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);

    }
}
