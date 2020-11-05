package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.permission.data.dto.PermissionSet;

public class File extends FileSystemItem{
    public File() {
    }

    public File(long id, String name, double size, long createdByUserId, long lastUpdated, FileSystemType type, PermissionSet permissionSet) {
        super(id, name, size, createdByUserId, lastUpdated, type, permissionSet);
    }
}