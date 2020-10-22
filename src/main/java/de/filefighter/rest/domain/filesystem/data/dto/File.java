package de.filefighter.rest.domain.filesystem.data.dto;

import de.filefighter.rest.domain.filesystem.type.FileSystemType;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.role.Role;
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
    private Role[] visibleForRoles;
    private Role[] editableForRoles;
    private User[] visibleForUsers;
    private User[] editableForUsers;
}