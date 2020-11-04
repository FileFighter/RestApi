package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemHealthBusinessService {

    private final UserBusinessService userBusinessService;
    private final long serverStartedAt;

    public SystemHealthBusinessService(UserBusinessService userBusinessService) {
        this.userBusinessService = userBusinessService;
        this.serverStartedAt = this.getCurrentEpochSeconds();
    }

    public SystemHealth getCurrentSystemHealthInfo(){
        long currentEpoch = getCurrentEpochSeconds();
        return SystemHealth.builder()
                .uptimeInSeconds(currentEpoch - serverStartedAt)
                .userCount(userBusinessService.getUserCount())
                .build();
    }

    public long getCurrentEpochSeconds(){
        return Instant.now().getEpochSecond();
    }
}
