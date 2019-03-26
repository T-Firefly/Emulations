package com.firefly.emulationstation.utils;

import android.view.InputDevice;
import android.view.MotionEvent;

import com.firefly.emulationstation.data.bean.GamepadButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by rany on 17-11-16.
 */

public class GamePadHelper {
    public static Observable<List<InputDevice>> checkGamePad() {
        return Observable.create(new ObservableOnSubscribe<List<InputDevice>>() {
            @Override
            public void subscribe(ObservableEmitter<List<InputDevice>> e) throws Exception {
                int[] deviceIds = InputDevice.getDeviceIds();
                List<InputDevice> inputDevices = new ArrayList<>();

                for (int deviceId : deviceIds) {
                    InputDevice device = InputDevice.getDevice(deviceId);

                    if (((device.getSources() & InputDevice.SOURCE_GAMEPAD)
                                != InputDevice.SOURCE_GAMEPAD ||
                            (device.getSources() & InputDevice.SOURCE_JOYSTICK)
                                    != InputDevice.SOURCE_JOYSTICK) ||
                            getConfigPath(device.getName()) != null) {
                        continue;
                    }

                    inputDevices.add(device);
                }
                e.onNext(inputDevices);
                e.onComplete();
            }
        });
    }

    private static String getConfigPath(String name) throws IOException {
        String autoConfigsPath;
        String inputDriver;
        try {
            autoConfigsPath = RetroConfigHelper.getConfigValue("joypad_autoconfig_dir");
            inputDriver = RetroConfigHelper.getConfigValue("input_driver");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        File file = new File(autoConfigsPath, inputDriver);
        if (!file.exists()) {
            throw new FileNotFoundException("Auto configs for " + inputDriver + " is not exist.");
        }

        File files[] = file.listFiles();
        for (File f : files) {
            String deviceName =
                    RetroConfigHelper.getConfigValueFromFile(f.getPath(), "input_device");
            if (name.equals(deviceName)) {
                return f.getPath();
            }
        }

        return null;
    }

    public static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    public static Observable<Boolean> saveGamepadMap(final GamepadButton gamepadButton) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if (gamepadButton == null ||
                        gamepadButton.getDeviceName() == null ||
                        gamepadButton.getDeviceName().isEmpty() ||
                        gamepadButton.getDriver() == null ||
                        gamepadButton.getDriver().isEmpty()) {
                    e.onError(new Throwable("The params is invalid."));
                    return;
                }

                File gamepadFile;
                String configPath = getConfigPath(gamepadButton.getDeviceName());
                if (configPath != null) {
                    gamepadFile = new File(configPath);
                } else {
                    String autoConfigPath = RetroConfigHelper
                            .getConfigValue("joypad_autoconfig_dir");
                    gamepadFile = new File(autoConfigPath,
                            gamepadButton.getDriver() + "/" +
                                    gamepadButton.getDeviceName() + ".cfg");
                }


                if (!gamepadFile.exists()) {
                    gamepadFile.getParentFile().mkdirs();
                    gamepadFile.createNewFile();
                }

                if (!gamepadFile.canWrite()) {
                    e.onError(new Throwable("Joypad auto config directory can't write."));
                    return;
                }

                boolean result = RetroConfigHelper
                        .saveConfigsToPath(gamepadFile.getPath(), gamepadButton.data);

                e.onNext(result);
            }
        });
    }

    public static Map<String, String> getGamepadConfigs(String name) throws IOException {
        String configPath = getConfigPath(name);

        if (configPath == null) {
            return null;
        }

        return RetroConfigHelper.getConfigsFromFile(configPath);
    }
}
