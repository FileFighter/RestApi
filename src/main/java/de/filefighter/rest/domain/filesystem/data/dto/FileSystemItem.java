package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.user.data.dto.User;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileSystemItem {

    @Builder.Default
    private long fileSystemId = -1;
    private String path;
    private String name;
    private boolean isShared;
    private double size;
    private User owner;
    private User lastUpdatedBy;
    private long lastUpdated;
    private FileSystemType type;
    private String mimeType;

}
