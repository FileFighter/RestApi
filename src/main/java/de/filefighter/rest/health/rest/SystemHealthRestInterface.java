package de.filefighter.rest.health.rest;

import de.filefighter.rest.health.data.SystemHealth;
import org.springframework.hateoas.EntityModel;

public interface SystemHealthRestInterface {
    EntityModel<SystemHealth> getSystemHealth();
}
