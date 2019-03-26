package com.firefly.emulationstation.settings;


import android.content.Intent;
import android.os.Bundle;
import android.support.v17.leanback.app.RowsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.Fragment;

import com.firefly.emulationstation.R;
import com.firefly.emulationstation.gamerepo.RepoActivity;
import com.firefly.emulationstation.settings.about.AboutActivity;
import com.firefly.emulationstation.settings.retroarch.RetroArchInfoActivity;
import com.firefly.emulationstation.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SettingsFragment extends RowsFragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private ArrayObjectAdapter mRowsAdapter;

    private static int[] SETTINGS_ITEMS_ICON = new int[] {
            R.drawable.gear,
            R.drawable.dpad,
            R.drawable.ic_extension_white,
            R.drawable.ic_get_app_white,
            R.drawable.ic_info_white,
    };
    private static int[] SETTINGS_ITEMS_NAME= new int[] {
            R.string.all_settings,
            R.string.settings_key_map,
            R.string.game_play,
            R.string.get_roms,
            R.string.about_app
    };
    private static int[] SETTINGS_ITEMS_XML = new int[] {
            R.xml.prefs,
            R.xml.keymap_prefs,
            0,
            0,
            0,
    };
    private static String[] SETTINGS_ITEMS_KEY= new String[] {
            null,
            "prefs_key_map",
            null,
            null,
            null,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        setAdapter(mRowsAdapter);

        setupUI();

        setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder,
                                      Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
                SettingsItem settingsItem = (SettingsItem)item;

                Intent intent = null;
                if (settingsItem.getXml() != 0) {
                    intent = new Intent(getActivity(), MainSettingsActivity.class);

                    intent.putExtra("xml", settingsItem.getXml());
                    if (settingsItem.getPrefsKey() == null) {
                        intent.putExtra("root", settingsItem.getPrefsKey());
                    }
                } else {
                    switch (settingsItem.getCardResId()) {
                        case R.drawable.ic_get_app_white:
                            intent = new Intent(getActivity(), RepoActivity.class);
                            break;
                        case R.drawable.ic_extension_white:
                            intent = new Intent(getActivity(), RetroArchInfoActivity.class);
                            break;
                        case R.drawable.ic_info_white:
                            intent = new Intent(getActivity(), AboutActivity.class);
                            break;
                    }
                }

                if (intent != null) {
                    startActivity(intent);
                } else {
                    Utils.showToast(getActivity(), R.string.not_implement);
                }
            }
        });
    }

    private void setupUI() {
        ArrayObjectAdapter settingsItemAdapter = new ArrayObjectAdapter(new SettingsItemPresenter());

        for (int i = 0; i < SETTINGS_ITEMS_ICON.length; ++i) {
            settingsItemAdapter.add(
                    new SettingsItem(
                            SETTINGS_ITEMS_ICON[i],
                            getString(SETTINGS_ITEMS_NAME[i]),
                            SETTINGS_ITEMS_XML[i],
                            SETTINGS_ITEMS_KEY[i]
                    )
            );
        }

        mRowsAdapter.add(new ListRow(settingsItemAdapter));
    }


}
