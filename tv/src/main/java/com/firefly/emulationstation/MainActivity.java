/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.firefly.emulationstation;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.firefly.emulationstation.services.VersionCheckService;

import dagger.android.DaggerActivity;

/*
 * MainActivity class that loads MainFragment
 */
public class MainActivity extends DaggerActivity {
    private static boolean isCheckRetroArchVersion = false;

    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        if (!isCheckRetroArchVersion) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Check RetroArch version
                    VersionCheckService.startActionCheckVersion(MainActivity.this);
                    isCheckRetroArchVersion = true;
                }
            }, 600);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        MainFragment fragment = (MainFragment)getFragmentManager()
                .findFragmentById(R.id.main_browse_fragment);
        if (fragment != null) {
            if (fragment.onKeyDown(keyCode, event)) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
