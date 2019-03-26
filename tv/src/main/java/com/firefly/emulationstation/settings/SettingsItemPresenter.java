package com.firefly.emulationstation.settings;

import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.emulationstation.R;

/**
 * Created by rany on 17-11-21.
 */

public class SettingsItemPresenter extends Presenter {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View cardView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_item, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        SettingsItem settingsItem = (SettingsItem) item;
        View cardView = viewHolder.view;
        final ImageView image = cardView.findViewById(R.id.icon);
        final TextView title= cardView.findViewById(R.id.title);

        image.setImageResource(settingsItem.getCardResId());
        title.setText(settingsItem.getName());
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }
}
