package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.data.SystemHealth;
import org.springframework.http.ResponseEntity;

public interface SystemHealthRestInterface {
    ResponseEntity<SystemHealth> getSystemHealth();
}
