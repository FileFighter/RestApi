package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class FileSystemItemsCouldNotBeUploadedException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "You are not allowed to upload the FileSystemItem or the folder does not exist.";

    public FileSystemItemsCouldNotBeUploadedException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public FileSystemItemsCouldNotBeUploadedException(long fsItemId) {
        super(ERROR_MESSAGE_PREFIX + " FileSystemId was " + fsItemId);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
