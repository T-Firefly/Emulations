package com.firefly.emulationstation.inputmap;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.data.bean.GamepadButton;
import com.firefly.emulationstation.inputmap.driver.AndroidDriver;
import com.firefly.emulationstation.inputmap.driver.InputDriver;
import com.firefly.emulationstation.utils.GamePadHelper;
import com.firefly.emulationstation.utils.RetroConfigHelper;
import com.firefly.emulationstation.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rany on 17-11-23.
 */

public class GamepadPresenter extends InputMapPresenter {
    private static final String TAG = GamepadPresenter.class.getSimpleName();

    private int[] deviceIds = null;
    private int index = 0;
    private InputDevice mCurrentDevice;
    private GamepadButton mGamepadButton;

    private CompositeDisposable mDisposables = new CompositeDisposable();


    public GamepadPresenter(InputMapFragment fragment, Context context) {
        super(fragment, context);
    }

    @Override
    void subscribe() {
        deviceIds = mFragment.getArguments()
                .getIntArray(InputMapActivity.PARAMS_DEVICE_IDS);
        index = mFragment.getArguments()
                .getInt(InputMapActivity.PARAMS_DEVICE_INDEX, 0);

        if (deviceIds == null || deviceIds.length <= index) {
            mFragment.finishGuidedStepFragments();
            return;
        }

        mCurrentDevice = InputDevice.getDevice(deviceIds[index]);
        mGamepadButton = new GamepadButton();

        mGamepadButton.setDeviceName(mCurrentDevice.getName());
        mGamepadButton.setProductId(String.valueOf(mCurrentDevice.getProductId()));
        mGamepadButton.setVendorId(String.valueOf(mCurrentDevice.getVendorId()));
        try {
            mGamepadButton.setDriver(RetroConfigHelper.getConfigValue("input_driver"));

            Map<String, String> keyMaps = GamePadHelper.getGamepadConfigs(
                    mGamepadButton.getDeviceName());
            if (keyMaps != null) {
                mGamepadButton.data.putAll(keyMaps);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mGamepadButton.data == null) {
            mGamepadButton.data = new HashMap<>();
        }
    }

    @Override
    void unsubscribe() {
        mDisposables.clear();
    }

    @Override
    public String getMapKeyValue(int i) {
        if (mGamepadButton.data == null ||
                mGamepadButton.data.isEmpty()) {
            return "";
        }

        String result = mGamepadButton.data.get(GamepadButton.getKey(i, false));

        if (result == null) {
            result = mGamepadButton.data.get(GamepadButton.getKey(i, true));
        }

        return result == null ? "" : result;
    }

    @Override
    int[] getLabelIds() {
        return GamepadButton.labelIds;
    }

    @Override
    boolean hasNextMap() {
        return (index+1) < deviceIds.length;
    }

    @Override
    GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        String breadcrumb = null;
        if ((mCurrentDevice.getSources() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            breadcrumb = mContext.getString(R.string.gamepad);
        }

        return new GuidanceStylist.Guidance(
                mCurrentDevice.getName(),
                mContext.getString(R.string.input_mapping),
                breadcrumb,
                null
        );
    }

    @Override
    public boolean onGenericMotion(View v, MotionEvent event,
                                   GuidedAction action, DialogFragment dialog) {
        InputDevice device = event.getDevice();
        InputDriver driver = null;

        if (device.getName().equals(mCurrentDevice.getName())) {
            if (mGamepadButton.getDriver().equals(AndroidDriver.NAME)) {
                driver = new AndroidDriver(event, device);
            } else {
                return false;
            }

            String key = driver.getSourceAxis();

            if (key != null && !key.isEmpty() && !isMapped(key)) {
                mGamepadButton.data.put(
                        GamepadButton.getKey((int) action.getId(), !key.startsWith("h")),
                        key);
                mFragment.updateActionUiAndMoveNext(action, key);
                dialog.dismiss();
            }

        }

        return true;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event,
                         GuidedAction action, DialogFragment dialog) {
        InputDevice device = event.getDevice();

        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                device.getName().equals(mCurrentDevice.getName())) {

            Log.d(TAG, "Press button " + keyCode);
            String keyCodeStr = String.valueOf(keyCode);
            if (!isMapped(keyCodeStr)) {
                mGamepadButton.data.put(
                        GamepadButton.getKey((int) action.getId(), false),
                        keyCodeStr);

                mFragment.updateActionUiAndMoveNext(action, keyCodeStr);
                dialog.dismiss();
            }

        }

        return true;
    }

    @Override
    public void saveConfigs() {
        Disposable disposable = GamePadHelper.saveGamepadMap(mGamepadButton)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean && !hasNextMap()) {
                            mFragment.setResultAndExit();
                        } else {
                            mFragment.startNextMap(deviceIds,  index + 1);
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
        if (hasNextMap()) {
            mFragment.startNextMap(deviceIds, index + 1);
        } else {
            mFragment.setResultAndExit();
        }
    }
}
