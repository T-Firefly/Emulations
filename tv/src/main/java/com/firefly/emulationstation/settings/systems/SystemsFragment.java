package com.firefly.emulationstation.settings.systems;


import android.content.Intent;
import android.os.Bundle;
import android.support.v17.preference.LeanbackPreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.commom.Constants;
import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.SystemsRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class SystemsFragment extends LeanbackPreferenceFragment {
    private static final String PARENT_KEY = "prefs_systems_screen";

    private PreferenceScreen mPrefParent;
    private List<GameSystem> mGameSystems;
    @Inject
    SystemsRepository mSystemsRepository;
    private boolean hasChanged = false;

    public SystemsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.systems_prefs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mPrefParent = (PreferenceScreen) findPreference(PARENT_KEY);

        mSystemsRepository.getGameSystems(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<List<GameSystem>>() {
                    @Override
                    public void accept(List<GameSystem> gameSystems) throws Exception {
                        loadSystems(gameSystems);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        getListView().requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (hasChanged) {
            mSystemsRepository.saveGameSystems();

            LocalBroadcastManager
                    .getInstance(getActivity())
                    .sendBroadcast(new Intent(Constants.BROADCAST_GAME_SYSTEM_CHANGED));
        }
    }

    private void loadSystems(List<GameSystem> systems) {
        mGameSystems = systems;

        for (GameSystem system : mGameSystems) {
            CheckBoxPreference preference = new CheckBoxPreference(mPrefParent.getContext());

            preference.setTitle(system.getName());
            preference.setChecked(system.isEnable());
            preference.getExtras();

            mPrefParent.addPreference(preference);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference pref = (CheckBoxPreference) preference;

            for (GameSystem system : mGameSystems) {
                if (pref.getTitle().equals(system.getName())) {
                    system.setEnable(pref.isChecked());
                    hasChanged = true;

                    return true;
                }
            }
        }

        return super.onPreferenceTreeClick(preference);
    }
}
