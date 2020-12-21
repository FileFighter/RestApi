package de.filefighter.rest.domain.filesystem.exceptions;

public class FileSystemContentsNotAccessibleException extends RuntimeException {

    public FileSystemContentsNotAccessibleException() {
        super("Folder does not exist, or you are not allowed to see the folder.");
    }

    public FileSystemContentsNotAccessibleException(String reason) {
        super("Folder contents could not be displayed. "+reason);
    }
}
