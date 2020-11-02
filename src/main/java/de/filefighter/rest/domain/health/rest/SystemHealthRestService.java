package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.business.SystemHealthBusinessService;
import de.filefighter.rest.domain.health.data.SystemHealth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SystemHealthRestService implements SystemHealthRestInterface {

    private final SystemHealthBusinessService systemHealthBusinessService;

    public SystemHealthRestService(SystemHealthBusinessService systemHealthBusinessService) {
        this.systemHealthBusinessService = systemHealthBusinessService;
    }

    @Override
    public ResponseEntity<SystemHealth> getSystemHealth() {
        return new ResponseEntity<>(systemHealthBusinessService.getCurrentSystemHealthInfo(), HttpStatus.OK);
    }
}
