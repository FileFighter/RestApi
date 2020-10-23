package de.filefighter.rest.domain.permission.rest;

import de.filefighter.rest.domain.permission.data.dto.PermissionSet;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class PermissionRestService implements PermissionRestServiceInterface{
    @Override
    public EntityModel<PermissionSet> getPermissionSetByIdAndToken(long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<PermissionSet> setPermissionSetByIdAndToken(PermissionSet newPermissionSet, long fsItemId, String accessToken) {
        return null;
    }

    @Override
    public EntityModel<PermissionSet> updatePermissionSetByIdAndToken(PermissionSet updatedPermissionSet, long fsItemId, String accessToken) {
        return null;
    }
}
