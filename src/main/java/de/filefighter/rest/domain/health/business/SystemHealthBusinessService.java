package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.domain.health.data.SystemHealth.DataIntegrity;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemHealthBusinessService {

    private final UserBusinessService userBusinessService;
    private final AccessTokenBusinessService accessTokenBusinessService;
    private final long serverStartedAt;

    @Value("${filefighter.version}")
    String version;

    public SystemHealthBusinessService(UserBusinessService userBusinessService, AccessTokenBusinessService accessTokenBusinessService) {
        this.userBusinessService = userBusinessService;
        this.accessTokenBusinessService = accessTokenBusinessService;
        this.serverStartedAt = this.getCurrentEpochSeconds();
    }

    public SystemHealth getCurrentSystemHealthInfo(){
        long currentEpoch = getCurrentEpochSeconds();
        return SystemHealth.builder()
                .uptimeInSeconds(currentEpoch - serverStartedAt)
                .userCount(userBusinessService.getUserCount())
                .dataIntegrity(calculateDataIntegrity())
                .version("v"+this.version)
                .build();
    }

    private DataIntegrity calculateDataIntegrity() {
        long userCount = userBusinessService.getUserCount();
        long accessTokenCount = accessTokenBusinessService.getAccessTokenCount();

        // Risk / Unstable Cases.
        if(userCount < accessTokenCount){
            return DataIntegrity.POSSIBLE_RISK;
        }

        return DataIntegrity.STABLE;
    }

    public long getCurrentEpochSeconds(){
        return Instant.now().getEpochSecond();
    }
}
