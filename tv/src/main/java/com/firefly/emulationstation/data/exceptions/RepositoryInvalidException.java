package com.firefly.emulationstation.data.exceptions;

/**
 * Created by rany on 18-5-16.
 */

public class RepositoryInvalidException extends ESException {
    public RepositoryInvalidException(String msg) {
        super(REPOSITORY_INVALID, msg);
    }

    public RepositoryInvalidException() {
        this("Repository invalid.");
    }
}
