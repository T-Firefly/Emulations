package com.firefly.emulationstation.data.exceptions;

/**
 * Created by rany on 18-5-16.
 */

public class RepositoryExistsException extends ESException {
    public RepositoryExistsException(String msg) {
        super(REPOSITORY_ALREADY_EXISTS, msg);
    }

    public RepositoryExistsException() {
        this("Repo already exists.");
    }
}
