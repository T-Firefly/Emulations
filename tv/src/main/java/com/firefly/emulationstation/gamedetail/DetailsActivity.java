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

package com.firefly.emulationstation.gamedetail;

import android.os.Bundle;

import com.firefly.emulationstation.R;

import dagger.android.DaggerActivity;

/*
 * Details activity class that loads LeanbackDetailsFragment class
 */
public class DetailsActivity extends DaggerActivity {
    public static final String SHARED_ELEMENT_NAME = "hero";
    public static final String GAME = "Game";
    public static final String SYSTEM = "system";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }

}
