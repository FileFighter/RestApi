package de.filefighter.rest.domain.filesystem.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileSystemUpload {
    private String path;
    private String name;
    private String mimeType;
    private double size;
}
