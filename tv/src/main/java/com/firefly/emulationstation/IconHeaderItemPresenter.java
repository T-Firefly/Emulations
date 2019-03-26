package com.firefly.emulationstation;

import android.support.v17.leanback.widget.PageRow;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.firefly.emulationstation.gamelist.GameHeaderItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rany on 18-4-18.
 */

class IconHeaderItemPresenter extends RowHeaderPresenter {
    private static final String TAG = IconHeaderItemPresenter.class.getSimpleName();

    private Map<String, Integer> iconsMap = new HashMap<String, Integer>() {{
        put("3do", R.drawable.ic_videogame_asset_grey);
        put("amiga", R.drawable.ic_videogame_asset_grey);
        put("amstradcpc", R.drawable.ic_videogame_asset_grey);
// missing apple2
        put("arcade", R.drawable.arcade);
// missing atari 800
        put("atari2600", R.drawable.ic_videogame_asset_grey);
        put("atari5200", R.drawable.ic_videogame_asset_grey);
        put("atari7800", R.drawable.ic_videogame_asset_grey);
        put("atarijaguar", R.drawable.ic_videogame_asset_grey);
        put("atarijaguarcd", R.drawable.ic_videogame_asset_grey);
        put("atarilynx", R.drawable.ic_videogame_asset_grey);
// missing atari ST/STE/Falcon
        put("atarixe", R.drawable.ic_videogame_asset_grey);
        put("colecovision", R.drawable.ic_videogame_asset_grey);
        put("c64", R.drawable.ic_videogame_asset_grey);
        put("intellivision", R.drawable.ic_videogame_asset_grey);
        put("macintosh", R.drawable.ic_videogame_asset_grey);
        put("xbox", R.drawable.ic_videogame_asset_grey);
        put("xbox360", R.drawable.ic_videogame_asset_grey);
        put("msx", R.drawable.ic_videogame_asset_grey);
        put("neogeo", R.drawable.ic_videogame_asset_grey);
        put("ngp", R.drawable.ic_videogame_asset_grey);
        put("ngpc", R.drawable.ic_videogame_asset_grey);
        put("n3ds", R.drawable.ic_videogame_asset_grey);
        put("n64", R.drawable.n64);
        put("nds", R.drawable.ic_videogame_asset_grey);
        put("fds", R.drawable.ic_videogame_asset_grey);
        put("nes", R.drawable.nes);
        put("gb", R.drawable.gameboy);
        put("gba", R.drawable.ic_videogame_asset_grey);
        put("gbc", R.drawable.gameboyc);
        put("gc", R.drawable.ic_videogame_asset_grey);
        put("wii", R.drawable.ic_videogame_asset_grey);
        put("wiiu", R.drawable.ic_videogame_asset_grey);
        put("virtualboy", R.drawable.ic_videogame_asset_grey);
        put("gameandwatch", R.drawable.ic_videogame_asset_grey);
        put("pc", R.drawable.pc);
        put("sega32x", R.drawable.ic_videogame_asset_grey);
        put("segacd", R.drawable.ic_videogame_asset_grey);
        put("dreamcast", R.drawable.ic_videogame_asset_grey);
        put("gamegear", R.drawable.ic_videogame_asset_grey);
        put("genesis", R.drawable.ic_videogame_asset_grey);
        put("mastersystem", R.drawable.ic_videogame_asset_grey);
        put("megadrive", R.drawable.ic_videogame_asset_grey);
        put("saturn", R.drawable.ic_videogame_asset_grey);
        put("sg-1000", R.drawable.ic_videogame_asset_grey);
        put("psx", R.drawable.ic_videogame_asset_grey);
        put("ps2", R.drawable.ic_videogame_asset_grey);
        put("ps3", R.drawable.ic_videogame_asset_grey);
        put("ps4", R.drawable.ic_videogame_asset_grey);
        put("psvita", R.drawable.ic_videogame_asset_grey);
        put("psp", R.drawable.ic_videogame_asset_grey);
        put("snes", R.drawable.ic_videogame_asset_grey);
        put("pcengine", R.drawable.ic_videogame_asset_grey);
        put("wonderswan", R.drawable.ic_videogame_asset_grey);
        put("wonderswancolor", R.drawable.ic_videogame_asset_grey);
        put("zxspectrum", R.drawable.ic_videogame_asset_grey);
        put("videopac", R.drawable.ic_videogame_asset_grey);
        put("vectrex", R.drawable.ic_videogame_asset_grey);
        put("trs-80", R.drawable.ic_videogame_asset_grey);
        put("coco", R.drawable.ic_videogame_asset_grey);
    }};

    private Map<String, Integer> iconsMapFocus = new HashMap<String, Integer>() {{
        put("3do", R.drawable.ic_videogame_asset_white);
        put("amiga", R.drawable.ic_videogame_asset_white);
        put("amstradcpc", R.drawable.ic_videogame_asset_white);
// missing apple2
        put("arcade", R.drawable.arcade_focus);
// missing atari 800
        put("atari2600", R.drawable.ic_videogame_asset_white);
        put("atari5200", R.drawable.ic_videogame_asset_white);
        put("atari7800", R.drawable.ic_videogame_asset_white);
        put("atarijaguar", R.drawable.ic_videogame_asset_white);
        put("atarijaguarcd", R.drawable.ic_videogame_asset_white);
        put("atarilynx", R.drawable.ic_videogame_asset_white);
// missing atari ST/STE/Falcon
        put("atarixe", R.drawable.ic_videogame_asset_white);
        put("colecovision", R.drawable.ic_videogame_asset_white);
        put("c64", R.drawable.ic_videogame_asset_white);
        put("intellivision", R.drawable.ic_videogame_asset_white);
        put("macintosh", R.drawable.ic_videogame_asset_white);
        put("xbox", R.drawable.ic_videogame_asset_white);
        put("xbox360", R.drawable.ic_videogame_asset_white);
        put("msx", R.drawable.ic_videogame_asset_white);
        put("neogeo", R.drawable.ic_videogame_asset_white);
        put("ngp", R.drawable.ic_videogame_asset_white);
        put("ngpc", R.drawable.ic_videogame_asset_white);
        put("n3ds", R.drawable.ic_videogame_asset_white);
        put("n64", R.drawable.n64_focus);
        put("nds", R.drawable.ic_videogame_asset_white);
        put("fds", R.drawable.ic_videogame_asset_white);
        put("nes", R.drawable.nes_focus);
        put("gb", R.drawable.gameboy_focus);
        put("gba", R.drawable.ic_videogame_asset_white);
        put("gbc", R.drawable.gameboyc_focus);
        put("gc", R.drawable.ic_videogame_asset_white);
        put("wii", R.drawable.ic_videogame_asset_white);
        put("wiiu", R.drawable.ic_videogame_asset_white);
        put("virtualboy", R.drawable.ic_videogame_asset_white);
        put("gameandwatch", R.drawable.ic_videogame_asset_white);
        put("pc", R.drawable.pc_focus);
        put("sega32x", R.drawable.ic_videogame_asset_white);
        put("segacd", R.drawable.ic_videogame_asset_white);
        put("dreamcast", R.drawable.ic_videogame_asset_white);
        put("gamegear", R.drawable.ic_videogame_asset_white);
        put("genesis", R.drawable.ic_videogame_asset_white);
        put("mastersystem", R.drawable.ic_videogame_asset_white);
        put("megadrive", R.drawable.ic_videogame_asset_white);
        put("saturn", R.drawable.ic_videogame_asset_white);
        put("sg-1000", R.drawable.ic_videogame_asset_white);
        put("psx", R.drawable.ic_videogame_asset_white);
        put("ps2", R.drawable.ic_videogame_asset_white);
        put("ps3", R.drawable.ic_videogame_asset_white);
        put("ps4", R.drawable.ic_videogame_asset_white);
        put("psvita", R.drawable.ic_videogame_asset_white);
        put("psp", R.drawable.ic_videogame_asset_white);
        put("snes", R.drawable.ic_videogame_asset_white);
        put("pcengine", R.drawable.ic_videogame_asset_white);
        put("wonderswan", R.drawable.ic_videogame_asset_white);
        put("wonderswancolor", R.drawable.ic_videogame_asset_white);
        put("zxspectrum", R.drawable.ic_videogame_asset_white);
        put("videopac", R.drawable.ic_videogame_asset_white);
        put("vectrex", R.drawable.ic_videogame_asset_white);
        put("trs-80", R.drawable.ic_videogame_asset_white);
        put("coco", R.drawable.ic_videogame_asset_white);
    }};

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.icon_header_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        final GameHeaderItem data = (GameHeaderItem) ((PageRow)item).getHeaderItem();
        View view = viewHolder.view;

        TextView title = view.findViewById(R.id.title);
        final ImageView icon = view.findViewById(R.id.icon);

        setupParentView((View) view.getParent());
        title.setText(data.getName());

        if (data.getGameSystem() == null) {
            int nTmp;
            int fTmp;
            if (data.getId() == MainFragment.SETTINGS_HEADER_ID) {
                nTmp = R.drawable.ic_settings_gray;
                fTmp = R.drawable.ic_settings_white;
            } else if (data.getId() == MainFragment.STAR_HEADER_ID) {
                nTmp = R.drawable.ic_star_gray;
                fTmp = R.drawable.ic_star_white;
            } else {
                nTmp = R.drawable.ic_videogame_asset_grey;
                fTmp = R.drawable.ic_videogame_asset_white;
            }

            final int normal = nTmp;
            final int focus = fTmp;

            icon.setImageResource(normal);
            view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        icon.setImageResource(focus);
                    } else {
                        icon.setImageResource(normal);
                    }
                }
            });

        } else {
            final Integer normalResId = iconsMap.get(data.getGameSystem().getPlatformId());
            final Integer focusResId = iconsMapFocus.get(data.getGameSystem().getPlatformId());

            if (normalResId == null)
                return;

            icon.setImageResource(normalResId);

            if (focusResId != null) {
                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            icon.setImageResource(focusResId);
                        } else {
                            icon.setImageResource(normalResId);
                        }
                    }
                });
            } else {
                view.setOnFocusChangeListener(null);
            }
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

    }

    @Override
    protected void onSelectLevelChanged(ViewHolder holder) {
    }

    private void setupParentView(View parent) {
        LayoutParams lp = parent.getLayoutParams();

        if (lp == null) {
            lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        } else {
            lp.width = LayoutParams.MATCH_PARENT;
        }
        parent.setLayoutParams(lp);

    }
}
