package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(buildMethodName = "create")
public class File {
    private long id;
    private String name;
    private FileSystemType type;
    private double size;
    private long createdByUserId; //uploadedBy
    private boolean isPublic;
    private long lastUpdated;
    private PermissionSet permissionSet;
}