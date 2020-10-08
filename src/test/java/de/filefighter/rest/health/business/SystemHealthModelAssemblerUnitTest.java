package de.filefighter.rest.health.business;

import de.filefighter.rest.health.data.SystemHealth;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.*;

class SystemHealthModelAssemblerUnitTest {

    private SystemHealthModelAssembler systemHealthModelAssembler;

    @Before
    public void setUp(){
        systemHealthModelAssembler = new SystemHealthModelAssembler();
    }

    @Test
    void toModel() {
        SystemHealth systemHealth = SystemHealth.builder().uptimeInSeconds(420).create();
        EntityModel<SystemHealth> systemHealthEntityModel = systemHealthModelAssembler.toModel(systemHealth);

        assertEquals(systemHealth, systemHealthEntityModel.getContent());
        assertTrue(systemHealthEntityModel.getLink("self").isPresent());
        assertTrue(systemHealthEntityModel.getLink("ugabuga").isEmpty());
    }
}