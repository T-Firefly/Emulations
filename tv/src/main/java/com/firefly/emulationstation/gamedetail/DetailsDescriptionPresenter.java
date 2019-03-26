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

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.Game;
import com.firefly.emulationstation.utils.I18nHelper;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        Game game = (Game) item;

        if (game != null) {
            viewHolder.getTitle().setText(game.getDisplayName());
            viewHolder.getSubtitle().setText(game.getDeveloper());
            if (game.getDescription() != null) {
                viewHolder.getBody().setText(I18nHelper.getValueFromMap(game.getDescription()));
            } else {
                viewHolder.getBody().setText(R.string.no_description);
            }
        }
    }
}
