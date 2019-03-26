package com.firefly.emulationstation.settings.about;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.BaseActivity;

public class AboutActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        FragmentManager mFragmentManager = getSupportFragmentManager();

        mFragmentManager.beginTransaction()
                .add(R.id.container, new VersionFragment())
                .commit();
    }
}
