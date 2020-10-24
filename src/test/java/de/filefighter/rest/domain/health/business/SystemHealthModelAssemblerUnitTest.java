package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.health.data.SystemHealth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemHealthModelAssemblerUnitTest {

    private SystemHealthModelAssembler systemHealthModelAssembler;

    @BeforeEach
    void setUp(){
        systemHealthModelAssembler = new SystemHealthModelAssembler();
    }

    @Test
    void toModel() {
        SystemHealth systemHealth = SystemHealth.builder().uptimeInSeconds(420).create();
        EntityModel<SystemHealth> systemHealthEntityModel = systemHealthModelAssembler.toModel(systemHealth);

        assertEquals(systemHealth, systemHealthEntityModel.getContent());
        assertTrue(systemHealthEntityModel.getLink("self").isPresent());
        assertTrue(systemHealthEntityModel.getLink("thislinkdoesnotexist").isEmpty());
    }
}