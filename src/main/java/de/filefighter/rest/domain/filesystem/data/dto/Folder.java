package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.permission.data.dto.PermissionSet;

public class Folder extends FileSystemItem {
    private String path;

    public Folder() {
    }

    public Folder(long id, String name, double size, long createdByUserId, long lastUpdated, FileSystemType type, PermissionSet permissionSet, String path) {
        super(id, name, size, createdByUserId, lastUpdated, type, permissionSet);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
