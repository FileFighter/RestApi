package de.filefighter.rest.domain.filesystem.exceptions;

public class FileSystemItemNotFoundException extends RuntimeException {

    public FileSystemItemNotFoundException() {
        super("FileSystemItem could not be found or you are not allowed to view it.");
    }

    public FileSystemItemNotFoundException(long fsItemId) {
        super("FileSystemItem with id " + fsItemId + " could not be found or you are not allowed to view it.");
    }
}
