package com.firefly.emulationstation.data.exceptions;

/**
 * Created by arch on 18-6-8.
 */

public class RetroArchCoreNotExistsException extends ESException {
    public RetroArchCoreNotExistsException(String msg, Object ref) {
        super(RETROARCH_CORE_NOT_EXISTS, msg, ref);
    }

    public RetroArchCoreNotExistsException(String core) {
        this("Can't find such core: " + core, core);
    }
}
