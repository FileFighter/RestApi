package de.filefighter.rest.domain.filesystem.exceptions;

public class FileSystemItemCouldNotBeDeletedException extends RuntimeException {

    public FileSystemItemCouldNotBeDeletedException() {
        super("FileSystemEntity could not be deleted.");
    }

    public FileSystemItemCouldNotBeDeletedException(long fileSystemId) {
        super("FileSystemEntity with the fileSystemId " + fileSystemId + " could not be deleted.");
    }
}
