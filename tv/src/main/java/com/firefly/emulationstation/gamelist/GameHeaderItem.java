package com.firefly.emulationstation.gamelist;

import android.support.annotation.NonNull;
import android.support.v17.leanback.widget.HeaderItem;

import com.firefly.emulationstation.data.bean.GameSystem;

/**
 * Created by rany on 17-10-31.
 */

public class GameHeaderItem extends HeaderItem {
    private GameSystem mGameSystem;

    public GameHeaderItem(long id, String name) {
        super(id, name);

        mGameSystem = null;
    }

    public GameHeaderItem(String name, @NonNull GameSystem gameSystem) {
        super(name);

        mGameSystem = gameSystem;
    }

    public GameSystem getGameSystem() {
        return mGameSystem;
    }
}
