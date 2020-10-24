package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderMethodName = "create")
public class FileSystemItemUpdate {
    private String name;
    private FileSystemType type;
    private double size;
    private boolean isPublic;
}
