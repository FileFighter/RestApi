package de.filefighter.rest.domain.permission.data.dto;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.role.Role;
import lombok.Getter;

@Getter
public class PermissionSet {
    private final Role[] visibleForRoles;
    private final Role[] editableForRoles;
    private final User[] visibleForUsers;
    private final User[] editableForUsers;

    public PermissionSet(Role[] visibleForRoles, Role[] editableForRoles, User[] visibleForUsers, User[] editableForUsers) {
        this.visibleForRoles = visibleForRoles;
        this.editableForRoles = editableForRoles;
        this.visibleForUsers = visibleForUsers;
        this.editableForUsers = editableForUsers;
    }
}
