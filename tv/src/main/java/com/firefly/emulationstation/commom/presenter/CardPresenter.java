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

package com.firefly.emulationstation.commom.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.graphics.Palette;
import android.view.ViewGroup;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.GlideApp;
import com.firefly.emulationstation.commom.ImageCardView;
import com.firefly.emulationstation.data.bean.DownloadInfo;
import com.firefly.emulationstation.data.bean.Game;

import static com.firefly.emulationstation.commom.ImageCardView.HIDE_PROGRESS;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        ImageCardView cardView = new ImageCardView(parent.getContext());
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Game game = (Game) item;
        final ImageCardView cardView = (ImageCardView) viewHolder.view;
        final DownloadInfo downloadInfo = game.getDownloadInfo();
        final Context context = viewHolder.view.getContext();

        cardView.setTitle(game.getDisplayName());
        if (downloadInfo != null) {
            cardView.setProgress(downloadInfo);
        } else {
            cardView.setProgressVisible(false);
        }

        if (game.getStatus() == Game.STATUS_RECOMMENDED) {
            cardView.setStatus(true);
        } else {
            cardView.setStatus(false);
        }

        String cardImage = null;
        if (game.getCardImageUrl() != null) {
            cardImage = game.getCardImageUrl();
        } else {
            cardImage = game.getIcon();
        }

        final String url = cardImage;

        GlideApp.with(context)
                .asBitmap()
                .load(url)
                .error(R.drawable.game)
                .into(new BitmapImageViewTarget(cardView.getCardView()) {
                    @Override
                    public void onResourceReady(@NonNull final Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);

                        Palette p = Palette.from(resource).generate();
                        int defaultColor = context.getResources()
                                .getColor(android.R.color.white);
                        int dominantColor = p.getDominantColor(defaultColor);

                        cardView.getCardView().setBackgroundColor(dominantColor);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        cardView.getCardView().setBackgroundResource(android.R.color.white);
                    }
                });

    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setMainCardViewDrawable(null);
    }
}
