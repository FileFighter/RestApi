package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.business.SystemHealthBusinessService;
import de.filefighter.rest.domain.health.business.SystemHealthModelAssembler;
import de.filefighter.rest.domain.health.data.SystemHealth;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthRestService implements SystemHealthRestInterface {

    private final SystemHealthBusinessService systemHealthBusinessService;
    private final SystemHealthModelAssembler systemHealthModelAssembler;

    public SystemHealthRestService(SystemHealthBusinessService systemHealthBusinessService, SystemHealthModelAssembler systemHealthModelAssembler) {
        this.systemHealthBusinessService = systemHealthBusinessService;
        this.systemHealthModelAssembler = systemHealthModelAssembler;
    }

    @Override
    public EntityModel<SystemHealth> getSystemHealth() {
        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();
        return systemHealthModelAssembler.toModel(systemHealth);
    }
}
