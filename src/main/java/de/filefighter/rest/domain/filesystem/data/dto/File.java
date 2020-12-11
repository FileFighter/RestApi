package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;

public class File extends FileSystemItem {
    public File() {
    }

    public File(long fileSystemId, String name, double size, long createdByUserId, long lastUpdated, FileSystemType type) {
        super(fileSystemId, name, size, createdByUserId, lastUpdated, type);
    }
}