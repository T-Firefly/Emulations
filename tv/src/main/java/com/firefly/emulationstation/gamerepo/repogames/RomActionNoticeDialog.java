package com.firefly.emulationstation.gamerepo.repogames;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.GlideApp;
import com.firefly.emulationstation.data.bean.Game;

/**
 * Created by rany on 18-5-31.
 */

public class RomActionNoticeDialog extends DialogFragment {
    public static final String ARG_GAME = "game";
    public static final String ARG_NOTICE = "notice";

    View.OnClickListener mListener = null;

    private Game mGame;
    private String mNotice;

    private int mPTextId = -1;
    private int mNTextId = -1;

    public static RomActionNoticeDialog newInstance(Game game, String notice) {
        RomActionNoticeDialog dialog = new RomActionNoticeDialog();

        Bundle args = new Bundle();
        args.putSerializable(ARG_GAME, game);
        args.putString(ARG_NOTICE, notice);

        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        mGame = (Game) args.get(ARG_GAME);
        mNotice = args.getString(ARG_NOTICE);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.RomActionNoticeDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_rom_action_notice, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button cancelBtn = view.findViewById(R.id.button_cancel);
        Button okBtn = view.findViewById(R.id.button_ok);
        TextView nameView = view.findViewById(R.id.game_name);
        TextView noticeView = view.findViewById(R.id.notice);
        final ImageView boxartView = view.findViewById(R.id.game_boxart);

        cancelBtn.setOnClickListener(mListener);
        if (mNTextId != -1) {
            cancelBtn.setText(mNTextId);
        }
        okBtn.setOnClickListener(mListener);
        if (mPTextId != -1) {
            okBtn.setText(mPTextId);
        }

        nameView.setText(mGame.getDisplayName());
        noticeView.setText(mNotice);

        GlideApp.with(getActivity())
                .asBitmap()
                .load(mGame.getCardImageUrl())
                .placeholder(R.drawable.detail_card_img)
                .error(R.drawable.detail_card_img)
                .into(new BitmapImageViewTarget(boxartView) {
                    @Override
                    public void onResourceReady(@NonNull final Bitmap resource,
                                                @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);

                        Palette p = Palette.from(resource).generate();
                        int defaultColor = getResources()
                                .getColor(android.R.color.white);
                        int dominantColor = p.getDominantColor(defaultColor);

                        boxartView.setBackgroundColor(dominantColor);
                    }
                });
    }

    public void setListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public void setNegativeButtonText(int textId) {
        mNTextId = textId;
    }

    public void setPositiveButtonText(int textId) {
        mPTextId = textId;
    }
}