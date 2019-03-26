package com.firefly.emulationstation.settings;

import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v7.preference.Preference;

/**
 * Created by rany on 17-11-22.
 */

public class PrefFragment extends LeanbackPreferenceFragment {

    public PrefFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        String root = getArguments().getString("root", null);
        int prefResId = getArguments().getInt("preferenceResource");
        if (root == null) {
            addPreferencesFromResource(prefResId);
        } else {
            setPreferencesFromResource(prefResId, root);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        return super.onPreferenceTreeClick(preference);
    }
}