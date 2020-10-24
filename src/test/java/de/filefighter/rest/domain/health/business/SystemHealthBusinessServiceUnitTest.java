package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemHealthBusinessServiceUnitTest {

    private final UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private SystemHealthBusinessService systemHealthBusinessService;

    @BeforeEach
    void setUp() {
        systemHealthBusinessService = new SystemHealthBusinessService(userBusinessServiceMock);
    }

    @Test
    void getCurrentSystemHealthInfo() {
        long expectedUserCount = 420;

        when(userBusinessServiceMock.getUserCount()).thenReturn(expectedUserCount);

        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();

        assertTrue(systemHealth.getUptimeInSeconds() >= 0);
        assertEquals(expectedUserCount, systemHealth.getUserCount());
    }

    @Test
    void getCurrentEpochSecondsReturnsEpochSeconds() {
        long expectedSeconds = Instant.now().getEpochSecond();
        long epochSeconds = systemHealthBusinessService.getCurrentEpochSeconds();
        assertEquals(expectedSeconds, epochSeconds);
    }
}