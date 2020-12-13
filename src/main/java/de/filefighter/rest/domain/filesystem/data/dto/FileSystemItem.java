package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileSystemItem {

    private long fileSystemId;
    private String path;
    private String name;
    private boolean isShared;
    private double size;
    private long createdByUserId; //uploadedBy
    private long lastUpdated;
    private FileSystemType type;

}
