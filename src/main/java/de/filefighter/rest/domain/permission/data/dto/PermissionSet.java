package de.filefighter.rest.domain.permission.data.dto;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.role.Role;

public class PermissionSet {
    private Role[] visibleForRoles;
    private Role[] editableForRoles;
    private User[] visibleForUsers;
    private User[] editableForUsers;
}
