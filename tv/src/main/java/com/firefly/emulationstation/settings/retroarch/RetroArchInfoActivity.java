package com.firefly.emulationstation.settings.retroarch;

import android.os.Bundle;

import com.firefly.emulationstation.R;

import dagger.android.DaggerActivity;

public class RetroArchInfoActivity extends DaggerActivity {
    public static final String ARG_UPDATE_FLAG = "update_flag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retro_arch_info);
    }
}
