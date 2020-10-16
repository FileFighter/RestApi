package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.data.SystemHealth;
import org.springframework.hateoas.EntityModel;

public interface SystemHealthRestInterface {
    EntityModel<SystemHealth> getSystemHealth();
}
