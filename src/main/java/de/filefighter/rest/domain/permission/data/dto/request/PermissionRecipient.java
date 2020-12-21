package de.filefighter.rest.domain.permission.data.dto.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PermissionRecipient {

    private PermissionRecipientType permissionRecipientType;
    private long userOrGroupId;

}