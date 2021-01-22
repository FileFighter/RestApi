package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.filesystem.business.FileSystemBusinessService;
import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.domain.health.data.SystemHealth.DataIntegrity;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SystemHealthBusinessServiceUnitTest {

    private final UserBusinessService userBusinessServiceMock = mock(UserBusinessService.class);
    private final AccessTokenBusinessService accessTokenBusinessServiceMock = mock(AccessTokenBusinessService.class);
    private final FileSystemBusinessService fileSystemBusinessServiceMock = mock(FileSystemBusinessService.class);
    private final Environment environmentMock = mock(Environment.class);
    private SystemHealthBusinessService systemHealthBusinessService;

    @BeforeEach
    void setUp() {
        systemHealthBusinessService = new SystemHealthBusinessService(userBusinessServiceMock, accessTokenBusinessServiceMock, fileSystemBusinessServiceMock, environmentMock);
    }

    @Test
    void getCurrentSystemHealthInfo() {
        long expectedUserCount = 420;
        double expectedSize = 1234.532;

        when(userBusinessServiceMock.getUserCount()).thenReturn(expectedUserCount);
        when(fileSystemBusinessServiceMock.getTotalFileSize()).thenReturn(expectedSize);
        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{"test"});

        SystemHealth systemHealth = systemHealthBusinessService.getCurrentSystemHealthInfo();

        assertTrue(systemHealth.getUptimeInSeconds() >= 0);
        assertEquals(expectedSize, systemHealth.getUsedStorageInBytes());
        assertEquals(expectedUserCount, systemHealth.getUserCount());
    }

    @Test
    void getCurrentEpochSecondsReturnsEpochSeconds() {
        long expectedSeconds = Instant.now().getEpochSecond();
        long epochSeconds = systemHealthBusinessService.getCurrentEpochSeconds();
        assertEquals(expectedSeconds, epochSeconds);
    }

    @Test
    void calculateDataIntegrityReturnsStable() {
        when(userBusinessServiceMock.getUserCount()).thenReturn(2L);
        when(accessTokenBusinessServiceMock.getAccessTokenCount()).thenReturn(2L);
        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{"test"});

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
    void calculateDataIntegrityReturnsRisk() {
        when(userBusinessServiceMock.getUserCount()).thenReturn(2L);
        when(accessTokenBusinessServiceMock.getAccessTokenCount()).thenReturn(3L);
        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{"test"});

        DataIntegrity dataIntegrity = DataIntegrity.POSSIBLE_RISK;
        DataIntegrity actual = systemHealthBusinessService.getCurrentSystemHealthInfo().getDataIntegrity();
        assertEquals(dataIntegrity, actual);
    }

    @Test
    void getDeploymentStatusWorks() {
        String string0 = "dev";
        String string1 = "non-prod";
        String string2 = "stage";

        when(environmentMock.getActiveProfiles()).thenReturn(new String[]{string0, string1, string2});

        assertEquals(string0 + " " + string1 + " " + string2, systemHealthBusinessService.getDeploymentStatus());
    }
}