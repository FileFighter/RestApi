package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.health.data.SystemHealth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemHealthBusinessServiceUnitTest {

    private static SystemHealthBusinessService systemHealthBusinessService;

    @BeforeAll
    static void setUp() {
        systemHealthBusinessService = new SystemHealthBusinessService();
    }

    @Test
    void getCurrentSystemHealthInfo() {
        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();
        assertTrue(systemHealth.getUptimeInSeconds() >= 0);
    }

    @Test
    void getCurrentEpochSecondsReturnsEpochSeconds() {
        long expectedSeconds = Instant.now().getEpochSecond();
        long epochSeconds = systemHealthBusinessService.getCurrentEpochSeconds();
        assertEquals(expectedSeconds, epochSeconds);
    }
}