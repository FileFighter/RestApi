package de.filefighter.rest.domain.health.rest;

import de.filefighter.rest.domain.health.business.SystemHealthBusinessService;
import de.filefighter.rest.domain.health.business.SystemHealthModelAssembler;
import de.filefighter.rest.domain.health.data.SystemHealth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemHealthRestServiceUnitTest {

    private final SystemHealthBusinessService systemHealthBusinessServiceMock = mock(SystemHealthBusinessService.class);
    private final SystemHealthModelAssembler systemHealthModelAssemblerMock = mock(SystemHealthModelAssembler.class);
    private SystemHealthRestService systemHealthRestService;

    @BeforeEach
    public void setUp() {
        systemHealthRestService = new SystemHealthRestService(systemHealthBusinessServiceMock, systemHealthModelAssemblerMock);
    }

    @Test
    void getSystemHealth() {
        SystemHealth dummyHealth = SystemHealth.builder().uptimeInSeconds(420).create();
        EntityModel<SystemHealth> actualModel = EntityModel.of(dummyHealth);

        when(systemHealthBusinessServiceMock.getCurrentSystemHealthInfo()).thenReturn(dummyHealth);
        when(systemHealthModelAssemblerMock.toModel(dummyHealth)).thenReturn(actualModel);

        EntityModel<SystemHealth> expectedModel = systemHealthRestService.getSystemHealth();

        assertEquals(expectedModel, actualModel);
    }
}