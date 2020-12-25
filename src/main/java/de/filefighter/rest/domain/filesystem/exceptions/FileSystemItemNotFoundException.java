package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class FileSystemItemNotFoundException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "FileSystemItem could not be found or you are not allowed to view it.";

    public FileSystemItemNotFoundException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public FileSystemItemNotFoundException(long fsItemId) {
        super(ERROR_MESSAGE_PREFIX + " FileSystemId was " + fsItemId);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
