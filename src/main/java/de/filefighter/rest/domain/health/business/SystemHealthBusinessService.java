package de.filefighter.rest.domain.health.business;

import de.filefighter.rest.domain.filesystem.business.FileSystemHelperService;
import de.filefighter.rest.domain.health.data.SystemHealth;
import de.filefighter.rest.domain.health.data.SystemHealth.DataIntegrity;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class SystemHealthBusinessService {

    private final UserBusinessService userBusinessService;
    private final AccessTokenBusinessService accessTokenBusinessService;
    private final FileSystemHelperService fileSystemHelperService;
    private final Environment environment;

    private final long serverStartedAt;
    private DataIntegrity cachedIntegrity = DataIntegrity.STABLE;

    @Value("${filefighter.version}")
    String version;

    public SystemHealthBusinessService(UserBusinessService userBusinessService, AccessTokenBusinessService accessTokenBusinessService, FileSystemHelperService fileSystemHelperService, Environment environment) {
        this.userBusinessService = userBusinessService;
        this.accessTokenBusinessService = accessTokenBusinessService;
        this.fileSystemHelperService = fileSystemHelperService;
        this.environment = environment;
        this.serverStartedAt = this.getCurrentEpochSeconds();
    }

    public SystemHealth getCurrentSystemHealthInfo() {
        long currentEpoch = getCurrentEpochSeconds();
        return SystemHealth.builder()
                .uptimeInSeconds(currentEpoch - serverStartedAt)
                .inodeCount(this.fileSystemHelperService.getTotalInodeCount())
                .userCount(userBusinessService.getUserCount())
                .usedStorageInBytes(fileSystemHelperService.getTotalFileSize())
                .dataIntegrity(calculateDataIntegrity())
                .deployment(getDeploymentStatus())
                .version("v" + this.version)
                .build();
    }

    public String getDeploymentStatus() {
        String[] profiles = environment.getActiveProfiles();

        StringBuilder deploymentStatus = new StringBuilder();

        for (String profile : profiles) {
            deploymentStatus.append(profile).append(" ");
        }
        return deploymentStatus.toString().strip();
    }

    private DataIntegrity calculateDataIntegrity() {
        long userCount = userBusinessService.getUserCount();
        long accessTokenCount = accessTokenBusinessService.getAccessTokenCount();

        // Risk / Unstable Cases.
        if (userCount < accessTokenCount) {
            this.triggerIntegrityChange(DataIntegrity.POSSIBLE_RISK);
        }

        return cachedIntegrity;
    }

    public long getCurrentEpochSeconds() {
        return Instant.now().getEpochSecond();
    }

    public void triggerIntegrityChange(DataIntegrity integrity) {
        if (cachedIntegrity.getCode() < integrity.getCode()) {
            this.cachedIntegrity = integrity;
        }
    }
}
