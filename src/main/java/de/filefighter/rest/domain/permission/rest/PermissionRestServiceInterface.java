package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import org.springframework.hateoas.EntityModel;

public interface PermissionRestServiceInterface {
    EntityModel<PermissionSet> updatePermissionSetByIdAndToken(long fsItemId, String accessToken);
    EntityModel<PermissionSet> setPermissionSetByIdAndToken(PermissionSet newPermissionSet, long fsItemId, String accessToken);
    EntityModel<PermissionSet> updatePermissionSetByIdAndToken(PermissionSet updatedPermissionSet, long fsItemId, String accessToken);
}
