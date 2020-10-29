package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import de.filefighter.rest.domain.permission.data.dto.request.PermissionRequest;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.hateoas.EntityModel;

public interface PermissionRestServiceInterface {
    EntityModel<PermissionSet> getPermissionSetByIdAndToken(long fsItemId, String accessToken);
    EntityModel<ServerResponse> addUsersOrGroupsToPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken);
    EntityModel<ServerResponse> removeUsersOrGroupsFromPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken);
}
