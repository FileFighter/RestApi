package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.data.SystemHealth;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "SystemHealth", description = "SystemHealth Controller")
public class SystemHealthRestController {

    private final SystemHealthRestInterface healthRestService;

    public SystemHealthRestController(SystemHealthRestInterface healthRestService) {
        this.healthRestService = healthRestService;
    }

    @GetMapping("/health")
    public ResponseEntity<SystemHealth> getSystemHealthInfo() {
        return healthRestService.getSystemHealth();
    }
}
