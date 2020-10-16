package de.filefighter.rest.health.rest;

import de.filefighter.rest.health.business.SystemHealthBusinessService;
import de.filefighter.rest.health.business.SystemHealthModelAssembler;
import de.filefighter.rest.health.data.SystemHealth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemHealthRestServiceUnitTest {

    private static final SystemHealthBusinessService systemHealthBusinessServiceMock = mock(SystemHealthBusinessService.class);
    private static final SystemHealthModelAssembler systemHealthModelAssemblerMock = mock(SystemHealthModelAssembler.class);
    private static SystemHealthRestService systemHealthRestService;

    @BeforeAll
    public static void setUp() {
        systemHealthRestService = new SystemHealthRestService(systemHealthBusinessServiceMock, systemHealthModelAssemblerMock);
    }

    @Test
    void getSystemHealth() {
        SystemHealth dummyHealth = SystemHealth.builder().uptimeInSeconds(420).create();
        EntityModel<SystemHealth> actualModell = EntityModel.of(dummyHealth);

        when(systemHealthBusinessServiceMock.getCurrentSystemHealthInfo()).thenReturn(dummyHealth);
        when(systemHealthModelAssemblerMock.toModel(dummyHealth)).thenReturn(actualModell);

        EntityModel<SystemHealth> expectedModel = systemHealthRestService.getSystemHealth();

        assertEquals(expectedModel, actualModell);
    }
}