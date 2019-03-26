package com.firefly.emulationstation.gamerepo.data.bean;

import com.firefly.emulationstation.data.bean.GameSystem;

public final class Category {
    public final String name;
    public final GameSystem gameSystem;

    public Category(String name) {
        this(name, null);
    }

    public Category(String name, GameSystem gameSystem) {
        this.name = name;
        this.gameSystem = gameSystem;
    }

    public boolean hasSystem() {
        return gameSystem != null;
    }

    @Override
    public String toString() {
        return name;
    }
}
