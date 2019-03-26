package com.firefly.emulationstation.services.downloader;

/**
 * Created by rany on 18-3-24.
 */

public class FileExistsException extends Exception {
    private int mTaskId;

    public FileExistsException(int taskId, String msg) {
        super(msg);

        mTaskId = taskId;
    }

    public int getTaskId() {
        return mTaskId;
    }
}
