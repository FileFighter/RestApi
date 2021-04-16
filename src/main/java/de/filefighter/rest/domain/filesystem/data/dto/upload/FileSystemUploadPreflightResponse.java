package de.filefighter.rest.domain.filesystem.data.dto.upload;

import lombok.Data;

@Data
public class FileSystemUploadPreflightResponse {
    private final String name;
    private final String path;
    private final Boolean permissionIsSufficient;
    private final Boolean isFile;
    private final Boolean nameAlreadyInUse;
    private final Boolean nameIsValid;
}