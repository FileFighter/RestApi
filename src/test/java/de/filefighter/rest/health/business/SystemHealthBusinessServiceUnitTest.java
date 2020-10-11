package de.filefighter.rest.health.business;

import de.filefighter.rest.health.data.SystemHealth;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemHealthBusinessServiceUnitTest {

    private static SystemHealthBusinessService systemHealthBusinessService;

    @BeforeAll
    static void setUp() {
        systemHealthBusinessService = new SystemHealthBusinessService();
    }

    @Test
    void getCurrentSystemHealthInfo() {
        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();
        assertEquals(0, systemHealth.getUptimeInSeconds());
    }

    @Test
    void getCurrentEpochSecondsReturnsEpochSeconds() {
        long expectedSeconds = Instant.now().getEpochSecond();
        long epochSeconds = systemHealthBusinessService.getCurrentEpochSeconds();
        assertEquals(expectedSeconds, epochSeconds);
    }
}