package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import de.filefighter.rest.domain.permission.data.dto.request.PermissionRequest;
import de.filefighter.rest.rest.ServerResponse;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.filefighter.rest.configuration.RestConfiguration.*;

@RestController
@Api(value = "Permissions Controller", tags = {"Permissions"})
@RequestMapping(BASE_API_URI)
public class PermissionRestController {

    private final Logger LOG = LoggerFactory.getLogger(PermissionRestController.class);
    private final PermissionRestServiceInterface permissionsRestService;

    public PermissionRestController(PermissionRestServiceInterface permissionsRestService) {
        this.permissionsRestService = permissionsRestService;
    }

    //R
    @GetMapping(FS_BASE_URI+"{fsItemId}/permission")
    public ResponseEntity<PermissionSet> getPermissionSetForFileOrFolder(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Requested PermissionSet for FileSystemItem {}", fsItemId);
        return permissionsRestService.getPermissionSetByIdAndToken(fsItemId, accessToken);
    }

    //C U
    @PutMapping(FS_BASE_URI+"{fsItemId}/permission")
    public ResponseEntity<ServerResponse> addUsersOrGroupsToPermissionSetForFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody PermissionRequest permissionRequest,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Requested new User or Group permissions {} for Id {}.",permissionRequest, fsItemId);
        return permissionsRestService.addUsersOrGroupsToPermissionSetForFileOrFolderWithAccessToken(permissionRequest,fsItemId, accessToken);
    }

    //D
    @DeleteMapping(FS_BASE_URI+"{fsItemId}/permission")
    public ResponseEntity<ServerResponse> removeUsersOrGroupsFromPermissionSetForFileOrFolder(
            @PathVariable long fsItemId,
            @RequestBody PermissionRequest permissionRequest,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Requested removal of User or Group permissions {} for Id {}.",permissionRequest, fsItemId);
        return permissionsRestService.removeUsersOrGroupsFromPermissionSetForFileOrFolderWithAccessToken(permissionRequest,fsItemId, accessToken);
    }
}
