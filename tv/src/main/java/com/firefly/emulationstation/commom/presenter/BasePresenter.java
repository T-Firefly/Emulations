package com.firefly.emulationstation.commom.presenter;

/**
 * Created by rany on 17-12-18.
 */

public interface BasePresenter<T> {
    void subscribe(T view);

    void unsubscribe();
}
