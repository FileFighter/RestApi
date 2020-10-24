package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.data.SystemHealth;
import io.swagger.annotations.Api;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "System Health", tags = { "SystemHealth" })
public class SystemHealthRestController {

    private final SystemHealthRestInterface healthRestService;

    public SystemHealthRestController(SystemHealthRestInterface healthRestService) {
        this.healthRestService = healthRestService;
    }

    @GetMapping("/health")
    public EntityModel<SystemHealth> getSystemHealthInfo(){
        return healthRestService.getSystemHealth();
    }
}
