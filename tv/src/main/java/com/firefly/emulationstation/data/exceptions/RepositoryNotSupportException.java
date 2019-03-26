package com.firefly.emulationstation.data.exceptions;

/**
 * Created by rany on 18-5-17.
 */

public class RepositoryNotSupportException extends ESException {
    public RepositoryNotSupportException(String msg) {
        super(REPOSITORY_NOT_SUPPORT, msg);
    }

    public RepositoryNotSupportException() {
        this("Not support.");
    }
}
