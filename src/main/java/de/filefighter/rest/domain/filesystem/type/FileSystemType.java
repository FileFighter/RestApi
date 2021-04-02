package de.filefighter.rest.domain.filesystem.type;

public enum FileSystemType {
    UNDEFINED(-1),
    FOLDER(0),
    TEXT(1),
    IMAGE(2),
    AUDIO(3),
    VIDEO(4),
    APPLICATION(5);

    private final long id;

    FileSystemType(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
