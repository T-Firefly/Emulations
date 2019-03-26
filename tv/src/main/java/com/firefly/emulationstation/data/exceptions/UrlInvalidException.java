package com.firefly.emulationstation.data.exceptions;

/**
 * Created by arch on 18-6-4.
 */

public class UrlInvalidException extends ESException {
    public UrlInvalidException(String msg) {
        super(DOWNLOAD_URL_INVALID, msg);
    }

    public UrlInvalidException() {
        this("Url is invalid.");
    }
}
