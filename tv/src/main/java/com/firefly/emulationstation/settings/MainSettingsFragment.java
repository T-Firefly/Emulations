package com.firefly.emulationstation.settings;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.PreferenceDialogFragment;
import android.support.v14.preference.PreferenceFragment;
import android.support.v17.preference.LeanbackSettingsFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.settings.keymap.KeyMapSettingsFragment;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainSettingsFragment extends LeanbackSettingsFragment {
    private Map<Integer, Class> mSettings = new HashMap<Integer, Class>() {{
        put(R.xml.keymap_prefs, KeyMapSettingsFragment.class);
        put(R.xml.prefs, PrefFragment.class);
    }};

    private int mXml;
    private String mRoot;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getActivity().getIntent();

        mXml = intent.getIntExtra("xml", R.xml.prefs);
        mRoot = intent.getStringExtra("root");
    }

    @Override
    public void onPreferenceStartInitialScreen() {
        startPreferenceFragment(buildPreferenceFragment(mRoot));
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        final Fragment f =
                Fragment.instantiate(getActivity(), pref.getFragment(), pref.getExtras());
        f.setTargetFragment(caller, 0);
        if (f instanceof PreferenceFragment || f instanceof PreferenceDialogFragment) {
            startPreferenceFragment(f);
        } else {
            startImmersiveFragment(f);
        }
        return true;
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragment caller, PreferenceScreen pref) {
        PreferenceFragment frag = buildPreferenceFragment(pref.getKey());
        startPreferenceFragment(frag);
        return true;
    }

    private PreferenceFragment buildPreferenceFragment(String root) {
        PreferenceFragment fragment = null;

        try {
            fragment = (PreferenceFragment)mSettings.get(mXml).newInstance();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (fragment != null) {
            Bundle args = new Bundle();
            args.putInt("preferenceResource", mXml);
            args.putString("root", root);
            fragment.setArguments(args);
        }

        return fragment;
    }
}
