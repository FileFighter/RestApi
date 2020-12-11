package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;

public class Folder extends FileSystemItem {
    private String path;

    public Folder() {
    }

    public Folder(long id, String path, String name, double size, long createdByUserId, long lastUpdated) {
        super(id, name, size, createdByUserId, lastUpdated, FileSystemType.FOLDER);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
