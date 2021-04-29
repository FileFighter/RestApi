package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class FileSystemItemCouldNotBeUploadedException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "FileSystemEntity could not be uploaded.";

    public FileSystemItemCouldNotBeUploadedException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public FileSystemItemCouldNotBeUploadedException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}