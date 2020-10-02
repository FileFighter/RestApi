package de.filefighter.rest.health.rest;

import de.filefighter.rest.health.data.SystemHealth;
import io.swagger.annotations.Api;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "System Health", tags = { "SystemHealth" })
@RequestMapping("/health")
public class HealthRestController {

    private final HealthRestInterface healthRestService;

    public HealthRestController(HealthRestInterface healthRestService) {
        this.healthRestService = healthRestService;
    }

    @GetMapping("/")
    public EntityModel<SystemHealth> getSystemHealthInfo(){
        return healthRestService.getSystemHealth();
    }
}
