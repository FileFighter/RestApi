package de.filefighter.rest.health.business;

import de.filefighter.rest.health.data.SystemHealth;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemHealthBusinessService {

    private final long serverStartedAt = Instant.now().getEpochSecond();

    public SystemHealth getCurrentSystemHealthInfo(){
        long currentEpoch = Instant.now().getEpochSecond();
        return SystemHealth.builder()
                .uptimeInSeconds(currentEpoch - serverStartedAt)
                .create();
    }
}
