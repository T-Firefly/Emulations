package com.firefly.emulationstation.commom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firefly.emulationstation.R;

import dagger.android.DaggerActivity;

public abstract class BaseDialogActivity extends DaggerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(R.layout.activity_base_dialog);
        ViewGroup viewGroup = findViewById(R.id.main_content);

        View view = LayoutInflater.from(this)
                .inflate(layoutResID, viewGroup, true);
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        findViewById(R.id.title_container).setVisibility(View.VISIBLE);
        TextView titleView = findViewById(R.id.title);
        titleView.setText(title);
    }
}
