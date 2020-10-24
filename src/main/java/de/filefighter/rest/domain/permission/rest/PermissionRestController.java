package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.EntityModel;
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

    @GetMapping(FS_BASE_URI+"{fsItemId}/permission")
    public EntityModel<PermissionSet> getPermissionsOfFileOrFolder(
            @PathVariable long fsItemId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Requested PermissionSet for FileSystemItem {}", fsItemId);
        return permissionsRestService.getPermissionSetByIdAndToken(fsItemId, accessToken);
    }

    @PostMapping(FS_BASE_URI+"{fsItemId}/permission")
    public EntityModel<PermissionSet> setPermissionSetForId(
            @PathVariable long fsItemId,
            @RequestBody PermissionSet newPermissionSet,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Requested PermissionSet for FileSystemItem {}", fsItemId);
        return permissionsRestService.setPermissionSetByIdAndToken(newPermissionSet, fsItemId, accessToken);
    }

    @PutMapping(FS_BASE_URI+"{fsItemId}/permission")
    public EntityModel<PermissionSet> updatePermissionSetForId(
            @PathVariable long fsItemId,
            @RequestBody PermissionSet updatedPermissionSet,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken
    ){

        LOG.info("Requested PermissionSet for FileSystemItem {}", fsItemId);
        return permissionsRestService.updatePermissionSetByIdAndToken(updatedPermissionSet,fsItemId, accessToken);
    }
}
