package com.firefly.emulationstation.data.bean;

import android.view.KeyEvent;

import com.firefly.emulationstation.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rany on 17-11-23.
 */

public class KeyboardButton {

    public static String getKey(int index, String player) {
        if (index >= 0 && index < keys.length) {
            return String.format(keys[index], player);
        }

        return null;
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
    };

    private static final String[] keys = new String[] {
            "input_%s_up",
            "input_%s_down",
            "input_%s_left",
            "input_%s_right",
            "input_%s_start",
            "input_%s_select",
            "input_%s_a",
            "input_%s_b",
            "input_%s_x",
            "input_%s_y",
            "input_%s_l",
            "input_%s_r",
            "input_%s_l2",
            "input_%s_r2",
            "input_%s_l3",
            "input_%s_r3",
            "input_%s_l_x_minus",
            "input_%s_l_x_plus",
            "input_%s_l_y_minus",
            "input_%s_l_y_plus",
            "input_%s_r_x_minus",
            "input_%s_r_x_plus",
            "input_%s_r_y_minus",
            "input_%s_r_y_plus",
            "input_%s_turbo",
    };
    
    public static final Map<Integer, String> KEY_MAP = new HashMap<Integer, String>() {{
        put(KeyEvent.KEYCODE_DEL, "backspace");
        put(KeyEvent.KEYCODE_TAB, "tab");
        put(KeyEvent.KEYCODE_CLEAR, "clear");
        put(KeyEvent.KEYCODE_ENTER, "enter");
        put(KeyEvent.KEYCODE_BREAK, "pause");
        put(KeyEvent.KEYCODE_ESCAPE, "escape");
        put(KeyEvent.KEYCODE_SPACE, "space");
        put(KeyEvent.KEYCODE_APOSTROPHE, "quote");
        put(KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN, "leftparen");
        put(KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN, "rightparen");
        put(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, "asterisk");
        put(KeyEvent.KEYCODE_NUMPAD_ADD, "plus");
        put(KeyEvent.KEYCODE_COMMA, "comma");
        put(KeyEvent.KEYCODE_MINUS, "minus");
        put(KeyEvent.KEYCODE_PERIOD, "period");
        put(KeyEvent.KEYCODE_SLASH, "slash");
        put(KeyEvent.KEYCODE_0, "num0");
        put(KeyEvent.KEYCODE_1, "num1");
        put(KeyEvent.KEYCODE_2, "num2");
        put(KeyEvent.KEYCODE_3, "num3");
        put(KeyEvent.KEYCODE_4, "num4");
        put(KeyEvent.KEYCODE_5, "num5");
        put(KeyEvent.KEYCODE_6, "num6");
        put(KeyEvent.KEYCODE_7, "num7");
        put(KeyEvent.KEYCODE_8, "num8");
        put(KeyEvent.KEYCODE_9, "num9");
        put(KeyEvent.KEYCODE_SEMICOLON, "semicolon");
        put(KeyEvent.KEYCODE_EQUALS, "equals");
        put(KeyEvent.KEYCODE_LEFT_BRACKET, "leftbracket");
        put(KeyEvent.KEYCODE_BACKSLASH, "backslash");
        put(KeyEvent.KEYCODE_RIGHT_BRACKET, "rightbracket");
        put(KeyEvent.KEYCODE_GRAVE, "tilde");
        put(KeyEvent.KEYCODE_A, "a");
        put(KeyEvent.KEYCODE_B, "b");
        put(KeyEvent.KEYCODE_C, "c");
        put(KeyEvent.KEYCODE_D, "d");
        put(KeyEvent.KEYCODE_E, "e");
        put(KeyEvent.KEYCODE_F, "f");
        put(KeyEvent.KEYCODE_G, "g");
        put(KeyEvent.KEYCODE_H, "h");
        put(KeyEvent.KEYCODE_I, "i");
        put(KeyEvent.KEYCODE_J, "j");
        put(KeyEvent.KEYCODE_K, "k");
        put(KeyEvent.KEYCODE_L, "l");
        put(KeyEvent.KEYCODE_M, "m");
        put(KeyEvent.KEYCODE_N, "n");
        put(KeyEvent.KEYCODE_O, "o");
        put(KeyEvent.KEYCODE_P, "p");
        put(KeyEvent.KEYCODE_Q, "q");
        put(KeyEvent.KEYCODE_R, "r");
        put(KeyEvent.KEYCODE_S, "s");
        put(KeyEvent.KEYCODE_T, "t");
        put(KeyEvent.KEYCODE_U, "u");
        put(KeyEvent.KEYCODE_V, "v");
        put(KeyEvent.KEYCODE_W, "w");
        put(KeyEvent.KEYCODE_X, "x");
        put(KeyEvent.KEYCODE_Y, "y");
        put(KeyEvent.KEYCODE_Z, "z");
        put(KeyEvent.KEYCODE_DEL, "del");
        put(KeyEvent.KEYCODE_NUMPAD_0, "keypad0");
        put(KeyEvent.KEYCODE_NUMPAD_1, "keypad1");
        put(KeyEvent.KEYCODE_NUMPAD_2, "keypad2");
        put(KeyEvent.KEYCODE_NUMPAD_3, "keypad3");
        put(KeyEvent.KEYCODE_NUMPAD_4, "keypad4");
        put(KeyEvent.KEYCODE_NUMPAD_5, "keypad5");
        put(KeyEvent.KEYCODE_NUMPAD_6, "keypad6");
        put(KeyEvent.KEYCODE_NUMPAD_7, "keypad7");
        put(KeyEvent.KEYCODE_NUMPAD_8, "keypad8");
        put(KeyEvent.KEYCODE_NUMPAD_9, "keypad9");
        put(KeyEvent.KEYCODE_NUMPAD_DOT, "kp_period");
        put(KeyEvent.KEYCODE_NUMPAD_DIVIDE, "divide");
        put(KeyEvent.KEYCODE_NUMPAD_MULTIPLY, "multiply");
        put(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, "subtract");
        put(KeyEvent.KEYCODE_NUMPAD_ADD, "kp_plus");
        put(KeyEvent.KEYCODE_NUMPAD_ENTER, "kp_enter");
        put(KeyEvent.KEYCODE_NUMPAD_EQUALS, "kp_equals");
        put(KeyEvent.KEYCODE_DPAD_UP, "up");
        put(KeyEvent.KEYCODE_DPAD_DOWN, "down");
        put(KeyEvent.KEYCODE_DPAD_RIGHT, "right");
        put(KeyEvent.KEYCODE_DPAD_LEFT, "left");
        put(KeyEvent.KEYCODE_INSERT, "insert");
        put(KeyEvent.KEYCODE_MOVE_HOME, "home");
        put(KeyEvent.KEYCODE_MOVE_END, "end");
        put(KeyEvent.KEYCODE_PAGE_UP, "pageup");
        put(KeyEvent.KEYCODE_PAGE_DOWN, "pagedown");
        put(KeyEvent.KEYCODE_F1, "f1");
        put(KeyEvent.KEYCODE_F2, "f2");
        put(KeyEvent.KEYCODE_F3, "f3");
        put(KeyEvent.KEYCODE_F4, "f4");
        put(KeyEvent.KEYCODE_F5, "f5");
        put(KeyEvent.KEYCODE_F6, "f6");
        put(KeyEvent.KEYCODE_F7, "f7");
        put(KeyEvent.KEYCODE_F8, "f8");
        put(KeyEvent.KEYCODE_F9, "f9");
        put(KeyEvent.KEYCODE_F10, "f10");
        put(KeyEvent.KEYCODE_F11, "f11");
        put(KeyEvent.KEYCODE_F12, "f12");
        put(KeyEvent.KEYCODE_NUM_LOCK, "numlock");
        put(KeyEvent.KEYCODE_CAPS_LOCK, "capslock");
        put(KeyEvent.KEYCODE_SCROLL_LOCK, "scroll_lock");
        put(KeyEvent.KEYCODE_SHIFT_LEFT, "lshift");
        put(KeyEvent.KEYCODE_SHIFT_RIGHT, "rshift");
        put(KeyEvent.KEYCODE_CTRL_RIGHT, "rctrl");
        put(KeyEvent.KEYCODE_CTRL_LEFT, "ctrl");
        put(KeyEvent.KEYCODE_ALT_RIGHT, "ralt");
        put(KeyEvent.KEYCODE_ALT_LEFT, "alt");
    }};
}
