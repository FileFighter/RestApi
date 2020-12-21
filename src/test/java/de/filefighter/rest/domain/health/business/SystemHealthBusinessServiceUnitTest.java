package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.domain.health.data.SystemHealth.DataIntegrity;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
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
    private final AccessTokenBusinessService accessTokenBusinessServiceMock = mock(AccessTokenBusinessService.class);
    private final FileSystemBusinessService fileSystemBusinessService = mock(FileSystemBusinessService.class);
    private SystemHealthBusinessService systemHealthBusinessService;

    @BeforeEach
    void setUp() {
        systemHealthBusinessService = new SystemHealthBusinessService(userBusinessServiceMock, accessTokenBusinessServiceMock, fileSystemBusinessService);
    }

    @Test
    void getCurrentSystemHealthInfo() {
        long expectedUserCount = 420;
        double expectedSize = 1234.532;

        when(userBusinessServiceMock.getUserCount()).thenReturn(expectedUserCount);
        when(fileSystemBusinessService.getTotalFileSize()).thenReturn(expectedSize);

        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();

        assertTrue(systemHealth.getUptimeInSeconds() >= 0);
        assertEquals(expectedSize, systemHealth.getUsedStorageInMb());
        assertEquals(expectedUserCount, systemHealth.getUserCount());
    }

    @Test
    void getCurrentEpochSecondsReturnsEpochSeconds() {
        long expectedSeconds = Instant.now().getEpochSecond();
        long epochSeconds = systemHealthBusinessService.getCurrentEpochSeconds();
        assertEquals(expectedSeconds, epochSeconds);
    }

    @Test
    void calculateDataIntegrityReturnsStable(){
        when(userBusinessServiceMock.getUserCount()).thenReturn(2L);
        when(accessTokenBusinessServiceMock.getAccessTokenCount()).thenReturn(2L);

        DataIntegrity dataIntegrity = DataIntegrity.STABLE;
        DataIntegrity actual = systemHealthBusinessService.getCurrentSystemHealthInfo().getDataIntegrity();
        assertEquals(dataIntegrity, actual);

        when(userBusinessServiceMock.getUserCount()).thenReturn(3L);
        when(accessTokenBusinessServiceMock.getAccessTokenCount()).thenReturn(2L);

        dataIntegrity = DataIntegrity.STABLE;
        actual = systemHealthBusinessService.getCurrentSystemHealthInfo().getDataIntegrity();
        assertEquals(dataIntegrity, actual);
    }

    @Test
    void calculateDataIntegrityReturnsRisk(){
        when(userBusinessServiceMock.getUserCount()).thenReturn(2L);
        when(accessTokenBusinessServiceMock.getAccessTokenCount()).thenReturn(3L);

        DataIntegrity dataIntegrity = DataIntegrity.POSSIBLE_RISK;
        DataIntegrity actual = systemHealthBusinessService.getCurrentSystemHealthInfo().getDataIntegrity();
        assertEquals(dataIntegrity, actual);
    }
}