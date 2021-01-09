package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class FileSystemItemCouldNotBeDeletedException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "FileSystemEntity could not be deleted.";

    public FileSystemItemCouldNotBeDeletedException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public FileSystemItemCouldNotBeDeletedException(long fileSystemId) {
        super(ERROR_MESSAGE_PREFIX+" FileSystemId was "+fileSystemId);
    }

    public FileSystemItemCouldNotBeDeletedException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
