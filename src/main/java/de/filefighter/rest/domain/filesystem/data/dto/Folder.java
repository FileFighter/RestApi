package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.permission.data.dto.PermissionSet;

public class Folder extends FileSystemItem {
    private String path;

    public Folder() {
    }

    public Folder(long id, String path, String name, double size, long createdByUserId, long lastUpdated, PermissionSet permissionSet) {
        super(id, name, size, createdByUserId, lastUpdated, FileSystemType.FOLDER, permissionSet);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
