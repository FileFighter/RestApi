package de.filefighter.rest.domain.permission.data.dto;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.role.Groups;

public class PermissionSet {
    private Groups[] visibleForRoles;
    private Groups[] editableForRoles;
    private User[] visibleForUsers;
    private User[] editableForUsers;
}
