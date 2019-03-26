package com.firefly.emulationstation.inputmap;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.KeyboardButton;
import com.firefly.emulationstation.settings.keymap.KeyMapSettingsFragment;
import com.firefly.emulationstation.utils.RetroConfigHelper;
import com.firefly.emulationstation.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rany on 17-11-23.
 */

public class KeyboardPresenter extends InputMapPresenter {
    private static final String TAG = KeyMapSettingsFragment.class.getSimpleName();

    private String mPlayer;
    private Map<String, String> mKeyMapCache;
    private Map<String, String> mConfigs;

    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Inject
    public KeyboardPresenter(InputMapFragment fragment, Context context) {
        super(fragment, context);
    }

    @Override
    void subscribe() {
        mPlayer = mFragment.getArguments()
                .getString(InputMapActivity.PARAMS_PLAYER_INDEX, "player1");
        mKeyMapCache = new HashMap<>();

        try {
            mConfigs = RetroConfigHelper.getConfigs();
        } catch (IOException e) {
            e.printStackTrace();
            mFragment.finishGuidedStepFragments();
            return;
        }
    }

    @Override
    void unsubscribe() {
        mDisposables.clear();
    }

    @Override
    String getMapKeyValue(int i) {
        String value = mConfigs.get(KeyboardButton.getKey(i, mPlayer));
        return value.equals("nul") ? "" : value;
    }

    @Override
    int[] getLabelIds() {
        return KeyboardButton.labelIds;
    }

    @Override
    boolean hasNextMap() {
        return false;
    }

    @Override
    GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        return new GuidanceStylist.Guidance(
                String.format("Player %c", getPlayerX()),
                mContext.getString(R.string.key_map, getPlayerX()),
                mContext.getString(R.string.keyboard),
                null
        );
    }

    @Override
    boolean onGenericMotion(View v, MotionEvent event,
                            GuidedAction action, DialogFragment dialog) {
        return false;
    }

    @Override
    boolean onKey(View v, int keyCode, KeyEvent event,
                  GuidedAction action, DialogFragment dialog) {
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                (event.getSource() & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            String key = KeyboardButton.KEY_MAP.get(keyCode);
            if (key != null) {
                mKeyMapCache.put(KeyboardButton.getKey((int) action.getId(), mPlayer), key);
                mFragment.updateActionUiAndMoveNext(action, key);
                dialog.dismiss();
            }
        }

        return true;
    }

    @Override
    void saveConfigs() {
        mConfigs.putAll(mKeyMapCache);

        Disposable disposable = RetroConfigHelper.saveConfigs(mConfigs)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            mFragment.setResultAndExit();
                        } else {
                            Utils.showToast(mContext, R.string.save_gamepad_map_failed);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        Utils.showToast(mContext, R.string.save_gamepad_map_failed);
                    }
                });

        mDisposables.add(disposable);
    }

    @Override
    void cancel() {
        mFragment.finishGuidedStepFragments();
    }

    private char getPlayerX() {
        return mPlayer.charAt(mPlayer.length() - 1);
    }
}
