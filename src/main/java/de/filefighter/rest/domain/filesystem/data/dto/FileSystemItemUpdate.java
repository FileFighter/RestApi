package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileSystemItemUpdate {
    private String name;
    private FileSystemType type;
    private double size;
    private boolean isInRoot;
    private List<FileSystemItemUpdate> children;
}
