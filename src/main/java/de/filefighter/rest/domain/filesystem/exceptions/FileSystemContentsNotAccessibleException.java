package de.filefighter.rest.domain.filesystem.exceptions;

import de.filefighter.rest.domain.common.exceptions.FileFighterException;

public class FileSystemContentsNotAccessibleException extends RuntimeException implements FileFighterException {

    private static final String ERROR_MESSAGE_PREFIX = "Folder does not exist, or you are not allowed to see the folder.";

    public FileSystemContentsNotAccessibleException() {
        super(ERROR_MESSAGE_PREFIX);
    }

    public FileSystemContentsNotAccessibleException(String reason) {
        super(ERROR_MESSAGE_PREFIX + " " + reason);
    }

    public static String getErrorMessagePrefix() {
        return ERROR_MESSAGE_PREFIX;
    }
}
