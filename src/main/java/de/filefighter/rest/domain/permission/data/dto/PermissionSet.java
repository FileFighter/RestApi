package de.filefighter.rest.domain.permission.data.dto;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Group;
import lombok.Getter;

@Getter
public class PermissionSet {
    private final Group[] visibleForGroups;
    private final Group[] editableForGroups;
    private final User[] visibleForUsers;
    private final User[] editableForUsers;

    public PermissionSet(Group[] visibleForGroups, Group[] editableForGroups, User[] visibleForUsers, User[] editableForUsers) {
        this.visibleForGroups = visibleForGroups;
        this.editableForGroups = editableForGroups;
        this.visibleForUsers = visibleForUsers;
        this.editableForUsers = editableForUsers;
    }
}
