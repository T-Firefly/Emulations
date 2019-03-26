package com.firefly.emulationstation.data.exceptions;

/**
 * Created by rany on 18-5-16.
 */

public class RepositoryDownloadErrorException extends ESException {
    public RepositoryDownloadErrorException(String msg) {
        super(REPOSITORY_DOWNLOAD_ERROR, msg);
    }

    public RepositoryDownloadErrorException() {
        this("Download error.");
    }
}
