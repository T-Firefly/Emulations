package com.firefly.emulationstation.commom.view;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firefly.emulationstation.R;

/**
 * Created by rany on 18-5-16.
 */

public class ProgressDialog extends DialogFragment {
    private static final String ARG_MSG = "msg";

    private String mMsg = null;

    public static ProgressDialog newInstance(String msg) {
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setMessage(msg);

        return progressDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_loading, container, false);
        TextView textView = view.findViewById(R.id.msg);

        if (mMsg == null) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(mMsg);
        }

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return view;
    }

    public ProgressDialog setMessage(String message) {
        mMsg = message;

        return this;
    }
}
