package de.filefighter.rest.domain.filesystem.type;

public enum FileSystemType {
    UNDEFINED(-1),
    FOLDER(0),
    TEXT(1),
    PICTURE(2),
    PDF(3),
    AUDIO(4),
    VIDEO(5)

    private final long id;

    FileSystemType(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
