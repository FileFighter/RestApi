package de.filefighter.rest.health.business;

import de.filefighter.rest.health.data.SystemHealth;
import org.jetbrains.annotations.NotNull;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SystemHealthModelAssembler implements RepresentationModelAssembler<SystemHealth, EntityModel<SystemHealth>> {

    @Override
    public @NotNull EntityModel<SystemHealth> toModel(@NotNull SystemHealth entity) {
        return EntityModel.of(entity);
    }
}
