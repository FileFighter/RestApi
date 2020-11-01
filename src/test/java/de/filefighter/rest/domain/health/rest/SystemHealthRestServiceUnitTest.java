package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.business.SystemHealthBusinessService;
import de.filefighter.rest.domain.health.data.SystemHealth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemHealthRestServiceUnitTest {

    private final SystemHealthBusinessService systemHealthBusinessServiceMock = mock(SystemHealthBusinessService.class);
    private SystemHealthRestService systemHealthRestService;

    @BeforeEach
    public void setUp() {
        systemHealthRestService = new SystemHealthRestService(systemHealthBusinessServiceMock);
    }

    @Test
    void getSystemHealth() {
        SystemHealth dummyHealth = SystemHealth.builder().uptimeInSeconds(420).create();
        ResponseEntity<SystemHealth> expectedModel = new ResponseEntity<>(dummyHealth, HttpStatus.OK);

        when(systemHealthBusinessServiceMock.getCurrentSystemHealthInfo()).thenReturn(dummyHealth);

        ResponseEntity<SystemHealth> actualModel = systemHealthRestService.getSystemHealth();

        assertEquals(expectedModel, actualModel);
    }
}