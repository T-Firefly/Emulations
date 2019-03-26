package com.firefly.emulationstation.commom;

import android.view.KeyEvent;

/**
 * Created by rany on 17-11-7.
 */

public interface IKeyDownEvent {
    boolean onKeyDown(int keyCode, KeyEvent event);
}
