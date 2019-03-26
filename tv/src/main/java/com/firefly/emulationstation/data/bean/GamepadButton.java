package com.firefly.emulationstation.data.bean;

import com.firefly.emulationstation.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rany on 17-11-16.
 */

public class GamepadButton {
    private static String DEVICE_NAME_KEY = "input_device";
    private static String DRIVER_KEY = "input_driver";
    private static String VENDOR_ID_KEY = "input_vendor_id";
    private static String PRODUCT_ID_KEY = "input_vendor_id";

    public Map<String, String> data = new HashMap<>();

    public String getDeviceName() {
        return data.get(DEVICE_NAME_KEY);
    }

    public void setDeviceName(String deviceName) {
        data.put(DEVICE_NAME_KEY, deviceName);
    }

    public String getDriver() {
        return data.get(DRIVER_KEY);
    }

    public void setDriver(String driver) {
        data.put(DRIVER_KEY, driver);
    }

    public String getVendorId() {
        return data.get(VENDOR_ID_KEY);
    }

    public void setVendorId(String vendorId) {
        data.put(VENDOR_ID_KEY, vendorId);
    }

    public String getProductId() {
        return data.get(PRODUCT_ID_KEY);
    }

    public void setProductId(String productId) {
        data.put(PRODUCT_ID_KEY, productId);
    }

    public static String getKey(int index, boolean isAnalog) {
        if (index < 0 || index >= keys.length) {
            return null;
        }

        if (isAnalog) {
            return String.format("%s_axis", keys[index]);
        } else {
            return String.format("%s_btn", keys[index]);
        }
    }

    public static final int[] labelIds = new int[] {
            R.string.gamepad_pad_up,
            R.string.gamepad_pad_down,
            R.string.gamepad_pad_left,
            R.string.gamepad_pad_right,
            R.string.gamepad_start,
            R.string.gamepad_select,
            R.string.gamepad_a,
            R.string.gamepad_b,
            R.string.gamepad_x,
            R.string.gamepad_y,
            R.string.gamepad_left_shoulder,
            R.string.gamepad_right_shoulder,
            R.string.gamepad_left_trigger,
            R.string.gamepad_right_trigger,
            R.string.gamepad_left_thumb,
            R.string.gamepad_right_thumb,
            R.string.gamepad_left_analog_up,
            R.string.gamepad_left_analog_down,
            R.string.gamepad_left_analog_left,
            R.string.gamepad_left_analog_right,
            R.string.gamepad_right_analog_up,
            R.string.gamepad_right_analog_down,
            R.string.gamepad_right_analog_left,
            R.string.gamepad_right_analog_right,
//            R.string.gamepad_hotkey_enable
    };
    private static final String[] keys = new String[] {
            "input_up",
            "input_down",
            "input_left",
            "input_right",
            "input_start",
            "input_select",
            "input_a",
            "input_b",
            "input_x",
            "input_y",
            "input_l",
            "input_r",
            "input_l2",
            "input_r2",
            "input_l3",
            "input_r3",
            "input_l_y_minus",
            "input_l_y_plus",
            "input_l_x_minus",
            "input_l_x_plus",
            "input_r_y_minus",
            "input_r_y_plus",
            "input_r_x_minus",
            "input_r_x_plus",
            "input_menu_toggle",
    };
}
