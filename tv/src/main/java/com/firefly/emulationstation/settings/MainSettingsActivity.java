package com.firefly.emulationstation.settings;

import android.os.Bundle;

import com.firefly.emulationstation.R;

import dagger.android.DaggerActivity;

public class MainSettingsActivity extends DaggerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);
    }
}
