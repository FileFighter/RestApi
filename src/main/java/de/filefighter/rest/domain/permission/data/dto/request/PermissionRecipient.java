package de.filefighter.rest.domain.permission.data.dto.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PermissionRecipient{
    private final PermissionRecipientType permissionRecipientType;
    private final long userOrGroupId;

    private PermissionRecipient(PermissionRecipientType permissionRecipientType, long userOrGroupId) {
        this.permissionRecipientType = permissionRecipientType;
        this.userOrGroupId = userOrGroupId;
    }
}