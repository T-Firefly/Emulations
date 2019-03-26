package com.firefly.emulationstation.inputmap.driver;

import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.firefly.emulationstation.utils.GamePadHelper;

/**
 * Created by rany on 17-11-20.
 */

public class AndroidDriver implements InputDriver {
    private static final String TAG = AndroidDriver.class.getSimpleName();
    public static final String NAME = "android";

    private MotionEvent mMotionEvent;
    private InputDevice mInputDevice;

    public AndroidDriver(MotionEvent event, InputDevice device) {
        mMotionEvent = event;
        mInputDevice = device;
    }

    @Override
    public String getSourceAxis() {
        String key = "";

        float x = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_X, -1);
        float y = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_Y, -1);
        float z =  GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_Z, -1);
        float rz = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_RZ, -1);
        float hatx = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_HAT_X, -1);
        float haty = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_HAT_Y, -1);
        float ltrig = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_LTRIGGER, -1);
        float rtrig = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_RTRIGGER, -1);
        float brake = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_BRAKE, -1);
        float gas = GamePadHelper.getCenteredAxis(mMotionEvent, mInputDevice,
                MotionEvent.AXIS_GAS, -1);

        // for Joystick X
        if (x != 0) {
            Log.d(TAG, "X：" + x);
            if (Float.compare(x, 0) > 0) {
                key = "+0";
            } else {
                key = "-0";
            }
        } else if (y != 0) { // for Joystick Y
            Log.d(TAG, "Y：" + y);
            if (Float.compare(y, 0) > 0) {
                key = "+1";
            } else {
                key = "-1";
            }
        } else if (z != 0) {// for right Joystick X
            Log.d(TAG, "RX：" + z);
            if (Float.compare(z, 0) > 0) {
                key = "+2";
            } else {
                key = "-2";
            }
        } else if (rz != 0) { // for right Joystick Y
            Log.d(TAG, "RY：" + rz);
            if (Float.compare(rz, 0) > 0) {
                key = "+3";
            } else {
                key = "-3";
            }
        } else if (hatx != 0) { // for DPAD X
            Log.d(TAG, "HAT_X：" + hatx);
            if (Float.compare(hatx, 0) > 0) {
                key = "h0right";
            } else {
                key = "h0left";
            }
        } else if (haty != 0) { // for DPAD Y
            Log.d(TAG, "HAT_Y：" + haty);
            if (Float.compare(haty, 0) > 0) {
                key = "h0down";
            } else {
                key = "h0up";
            }
        } else if (rtrig != 0) {
            Log.d(TAG, "R Trigger：" + rtrig);
            if (Float.compare(rtrig, 0) > 0) {
                key = "+7";
            } else {
                key = "-7";
            }
        } else if (ltrig != 0) {
            Log.d(TAG, "L Trigger：" + ltrig);
            if (Float.compare(ltrig, 0) > 0) {
                key = "+6";
            } else {
                key = "-6";
            }
        } else if (brake != 0) {
            // TODO : true?
            Log.d(TAG, "BRAKE：" + brake);
            if (Float.compare(brake, 0) > 0) {
                key = "+8";
            } else {
                key = "-8";
            }
        } else if (gas != 0) {
            // TODO
            Log.d(TAG, "GAS：" + gas);
            if (Float.compare(gas, 0) > 0) {
                key = "+9";
            } else {
                key = "-9";
            }
        }

        return key;
    }

    @Override
    public String getDriverName() {
        return NAME;
    }
}
