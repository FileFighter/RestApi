package de.filefighter.rest.health.rest;

import de.filefighter.rest.health.business.SystemHealthBusinessService;
import de.filefighter.rest.health.business.SystemHealthModelAssembler;
import de.filefighter.rest.health.data.SystemHealth;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class HealthRestService implements HealthRestInterface{

    private final SystemHealthBusinessService systemHealthBusinessService;
    private final SystemHealthModelAssembler systemHealthModelAssembler;

    public HealthRestService(SystemHealthBusinessService systemHealthBusinessService, SystemHealthModelAssembler systemHealthModelAssembler) {
        this.systemHealthBusinessService = systemHealthBusinessService;
        this.systemHealthModelAssembler = systemHealthModelAssembler;
    }

    @Override
    public EntityModel<SystemHealth> getSystemHealth() {
        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();
        return systemHealthModelAssembler.toModel(systemHealth);
    }
}
