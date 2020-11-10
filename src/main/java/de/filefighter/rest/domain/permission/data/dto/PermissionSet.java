package de.filefighter.rest.domain.permission.data.dto;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Groups;
import lombok.Getter;

@Getter
public class PermissionSet {
    private final Groups[] visibleForGroups;
    private final Groups[] editableForGroups;
    private final User[] visibleForUsers;
    private final User[] editableForUsers;

    public PermissionSet(Groups[] visibleForGroups, Groups[] editableForGroups, User[] visibleForUsers, User[] editableForUsers) {
        this.visibleForGroups = visibleForGroups;
        this.editableForGroups = editableForGroups;
        this.visibleForUsers = visibleForUsers;
        this.editableForUsers = editableForUsers;
    }
}
