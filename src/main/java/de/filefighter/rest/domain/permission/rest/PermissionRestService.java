package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import de.filefighter.rest.domain.permission.data.dto.request.PermissionRequest;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PermissionRestService implements PermissionRestServiceInterface{
    @Override
    public ResponseEntity<PermissionSet> getPermissionSetByIdAndToken(long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> addUsersOrGroupsToPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> removeUsersOrGroupsFromPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken) {
        return null;
    }
}
