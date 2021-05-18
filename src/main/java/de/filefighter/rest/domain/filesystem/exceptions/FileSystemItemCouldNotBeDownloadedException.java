package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class FileSystemItemCouldNotBeDownloadedException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "The FileSystemItem could not be downloaded.";

    public FileSystemItemCouldNotBeDownloadedException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public FileSystemItemCouldNotBeDownloadedException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }

}
