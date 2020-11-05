package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileSystemItem {
    private long id;
    private String name;
    private double size;
    private long createdByUserId; //uploadedBy
    private long lastUpdated;
    private FileSystemType type;
    private PermissionSet permissionSet;

    protected FileSystemItem() {
    }

    public FileSystemItem(long id, String name, double size, long createdByUserId, long lastUpdated, FileSystemType type, PermissionSet permissionSet) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.createdByUserId = createdByUserId;
        this.lastUpdated = lastUpdated;
        this.type = type;
        this.permissionSet = permissionSet;
    }
}
