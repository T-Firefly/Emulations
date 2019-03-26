package com.firefly.emulationstation.data.exceptions;

import com.firefly.emulationstation.data.bean.Game;

/**
 * Created by rany on 18-5-5.
 */

public class GameExistsException extends ESException {

    public GameExistsException(String msg) {
        this(msg, null);
    }

    public GameExistsException(String msg, Object ref) {
        super(GAME_EXISTS, msg, ref);
    }

    public Game getGame() {
        return (Game) getRef();
    }
}
