package com.firefly.emulationstation.data.exceptions;

public class RepositoryAlreadyNewestException extends ESException {
    public RepositoryAlreadyNewestException(String msg) {
        super(REPOSITORY_ALREADY_NEWEST, msg);
    }

    public RepositoryAlreadyNewestException() {
        super(REPOSITORY_ALREADY_NEWEST, "Repository is newest.");
    }
}
