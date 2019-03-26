package com.firefly.emulationstation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.firefly.emulationstation.data.bean.GameSystem;
import com.firefly.emulationstation.data.repository.SystemsRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MediaMountedReceiver extends BroadcastReceiver {
    @Inject
    SystemsRepository mSystemsRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);

        if (!Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())
                && !UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
            return;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mSystemsRepository.getGameSystems(true)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<List<GameSystem>>() {
                    @Override
                    public void accept(List<GameSystem> gameSystems) throws Exception {
                        // Nothing to do
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }
}
