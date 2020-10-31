package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import de.filefighter.rest.domain.permission.data.dto.request.PermissionRequest;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class PermissionRestService implements PermissionRestServiceInterface{
    @Override
    public EntityModel<PermissionSet> getPermissionSetByIdAndToken(long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<ServerResponse> addUsersOrGroupsToPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<ServerResponse> removeUsersOrGroupsFromPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken) {
        return null;
    }
}
