package com.firefly.emulationstation.settings.retroarch;

import android.content.Context;
import android.support.v17.leanback.widget.Presenter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.services.RetroArchDownloadService;

import java.util.Locale;

/**
 * Created by rany on 18-4-16.
 */

class WideCardPresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.retro_arch_info_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        View view = viewHolder.view;
        Item data = (Item) item;

        TextView title = view.findViewById(R.id.title);
        TextView status = view.findViewById(R.id.status);
//        TextView desc = view.findViewById(R.id.desc);
        ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        TextView progressPercent = view.findViewById(R.id.progress_percent);

        title.setText(data.name);
        status.setText(data.status);

        if (data.progress != -1) {
            view.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);
            progressBar.setProgress(data.progress);
            progressPercent.setText(String.format(Locale.getDefault(), "%d%%", data.progress));
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

    }

    static class Item {
        String name;
        String description;
        String status;
        int progress;

        Item(String name, String status) {
            this(name, null, status, -1);
        }

        Item(String name, String description, String status, int progress) {
            this.name = name;
            this.description = description;
            this.status = status;
            this.progress = progress;
        }
    }
}
