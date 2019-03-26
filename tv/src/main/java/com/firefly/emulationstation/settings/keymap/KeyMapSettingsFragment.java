package com.firefly.emulationstation.settings.keymap;


import android.content.Intent;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.view.InputDevice;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.inputmap.InputMapActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class KeyMapSettingsFragment extends LeanbackPreferenceFragment {
    private static final String TAG = KeyMapSettingsFragment.class.getSimpleName();
    private static final String KEY_GAMEPAD_CONNECTED = "prefs_gamepad_connected";
    private static final String KEY_KEYBOARD_MAP = "prefs_keyboard_map";

    private PreferenceCategory mGamepadConnectedCategory;
    private PreferenceCategory mKeyboardMapCategory;

    private Map<String, InputDevice> mGamepads = new HashMap<>();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.keymap_prefs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGamepadConnectedCategory = (PreferenceCategory) findPreference(KEY_GAMEPAD_CONNECTED);
        mKeyboardMapCategory = (PreferenceCategory) findPreference(KEY_KEYBOARD_MAP);

        loadConnected();
        setPlayerMapList();
    }

    private void setPlayerMapList() {
        for (int i = 1; i <= Constants.SUPPORT_MAX_PLAYER; ++i) {
            Preference preference = new Preference(mKeyboardMapCategory.getContext());
            String name = getString(R.string.player_x, i);

            preference.setTitle(name);
            preference.setKey(String.format(Locale.US, "player%d", i));

            mKeyboardMapCategory.addPreference(preference);
        }
    }

    private void loadConnected() {
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int id : deviceIds) {
            InputDevice device = InputDevice.getDevice(id);
            int sources = device.getSources();

            if ((sources & InputDevice.SOURCE_JOYSTICK) != InputDevice.SOURCE_JOYSTICK ||
                    (sources & InputDevice.SOURCE_GAMEPAD) != InputDevice.SOURCE_GAMEPAD) {
                continue;
            }

            mGamepads.put(device.getName(), device);

            Preference preference = new Preference(mGamepadConnectedCategory.getContext());
            preference.setTitle(device.getName());
            preference.setKey(device.getName());

            mGamepadConnectedCategory.addPreference(preference);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        PreferenceGroup parent = preference.getParent();

        if (parent != null) {
            Intent intent = new Intent(getActivity(), InputMapActivity.class);

            if (parent.getKey().equals(KEY_GAMEPAD_CONNECTED)) {
                InputDevice device = mGamepads.get(preference.getKey());
                intent.putExtra(InputMapActivity.PARAMS_DEVICE_IDS, new int[]{device.getId()});
            } else if (parent.getKey().equals(KEY_KEYBOARD_MAP)) {
                intent.putExtra(InputMapActivity.PARAMS_PLAYER_INDEX, preference.getKey());
            }

            startActivity(intent);
        }

        return super.onPreferenceTreeClick(preference);
    }
}
