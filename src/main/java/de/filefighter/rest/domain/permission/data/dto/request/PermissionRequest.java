package de.filefighter.rest.domain.permission.data.dto.request;

import lombok.Data;

@Data
public class PermissionRequest {
    PermissionType permission;
    PermissionRecipient[] permissionRecipients;
}
