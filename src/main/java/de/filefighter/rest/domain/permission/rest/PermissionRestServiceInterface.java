package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import de.filefighter.rest.domain.permission.data.dto.request.PermissionRequest;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.ResponseEntity;

public interface PermissionRestServiceInterface {
    ResponseEntity<PermissionSet> getPermissionSetByIdAndToken(long fsItemId, String accessToken);
    ResponseEntity<ServerResponse> addUsersOrGroupsToPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken);
    ResponseEntity<ServerResponse> removeUsersOrGroupsFromPermissionSetForFileOrFolderWithAccessToken(PermissionRequest permissionRequest, long fsItemId, String accessToken);
}
