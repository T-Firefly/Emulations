package com.firefly.emulationstation.data.exceptions;

import android.support.annotation.IntDef;

/**
 * Created by rany on 18-5-3.
 */

public class ESException extends Throwable {
    @IntDef({
            GAME_EXISTS,
            NETWORK_TIMEOUT,
            REPOSITORY_ALREADY_EXISTS,
            REPOSITORY_INVALID,
            REPOSITORY_DOWNLOAD_ERROR,
            REPOSITORY_NOT_SUPPORT,
            DOWNLOAD_URL_INVALID,
            RETROARCH_CORE_NOT_EXISTS,
            REPOSITORY_ALREADY_NEWEST,
            RETROARCH_CAN_NOT_GET_INFO
    })
    public @interface Status {}

    public final static int GAME_EXISTS = 0;
    public final static int NETWORK_TIMEOUT = 1;
    public final static int REPOSITORY_ALREADY_EXISTS = 2;
    public final static int REPOSITORY_INVALID = 3;
    public final static int REPOSITORY_DOWNLOAD_ERROR = 4;
    public final static int REPOSITORY_NOT_SUPPORT = 5;
    public final static int DOWNLOAD_URL_INVALID = 6;
    public final static int RETROARCH_CORE_NOT_EXISTS = 7;
    public final static int REPOSITORY_ALREADY_NEWEST = 8;
    public final static int RETROARCH_CAN_NOT_GET_INFO = 9;

    @Status
    private int status;
    private Object ref;

    public ESException(@Status int status, String msg) {
        this(status, msg, null);
    }

    public ESException(@Status int status, String msg, Object ref) {
        super(msg);

        this.status = status;
        this.ref = ref;
    }

    public int getStatus() {
        return status;
    }

    public Object getRef() {
        return ref;
    }
}
