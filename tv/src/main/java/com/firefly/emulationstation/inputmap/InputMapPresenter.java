package com.firefly.emulationstation.inputmap;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import io.reactivex.Observable;

/**
 * Created by rany on 17-11-23.
 */

public abstract class InputMapPresenter {

    protected InputMapFragment mFragment;
    protected Context mContext;

    public InputMapPresenter(InputMapFragment fragment, Context context) {
        mFragment = fragment;
        mContext = context;

        mFragment.setPresenter(this);
    }

    /*
     * TODO: This is necessary?
     */
    protected boolean isMapped(String value) {
//        Collection<String> values = mGamepadButton.data.values();
//
//        for (String v : values) {
//            if (v.equals(value)) {
//                return true;
//            }
//        }

        return false;
    }

    abstract void subscribe();
    abstract void unsubscribe();
    abstract String getMapKeyValue(int i);
    abstract int[] getLabelIds();
    abstract boolean hasNextMap();
    abstract GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState);
    abstract boolean onGenericMotion(View v, MotionEvent event,
                                     GuidedAction action, DialogFragment dialog);
    abstract boolean onKey(View v, int keyCode, KeyEvent event,
                           GuidedAction action, DialogFragment dialog);
    abstract void saveConfigs();
    abstract void cancel();
}
