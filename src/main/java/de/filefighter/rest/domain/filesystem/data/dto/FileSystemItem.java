package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSystemItem {
    private long fileSystemId;
    private String name;
    private double size;
    private long createdByUserId; //uploadedBy
    private long lastUpdated;
    private FileSystemType type;

    protected FileSystemItem() {
    }

    public FileSystemItem(long fileSystemId, String name, double size, long createdByUserId, long lastUpdated, FileSystemType type) {
        this.fileSystemId = fileSystemId;
        this.name = name;
        this.size = size;
        this.createdByUserId = createdByUserId;
        this.lastUpdated = lastUpdated;
        this.type = type;
    }
}
