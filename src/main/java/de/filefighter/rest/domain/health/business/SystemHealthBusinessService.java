package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.health.data.SystemHealth;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemHealthBusinessService {

    private final long serverStartedAt;

    public SystemHealthBusinessService() {
        this.serverStartedAt = this.getCurrentEpochSeconds();
    }

    public SystemHealth getCurrentSystemHealthInfo(){
        long currentEpoch = getCurrentEpochSeconds();
        return SystemHealth.builder()
                .uptimeInSeconds(currentEpoch - serverStartedAt)
                .create();
    }

    public long getCurrentEpochSeconds(){
        return Instant.now().getEpochSecond();
    }
}
