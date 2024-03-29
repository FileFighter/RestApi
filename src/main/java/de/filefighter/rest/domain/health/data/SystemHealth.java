package de.filefighter.rest.domain.health.data;

import lombok.Builder;
import lombok.Data;

/**
 * This class is a representation of the json model.
 */

@Data
@Builder
public class SystemHealth {
    private final long uptimeInSeconds;
    private final long userCount;
    private final long inodeCount;
    private final double usedStorageInBytes;
    private final DataIntegrity dataIntegrity;
    private final String deployment;
    private final String version;

    public enum DataIntegrity {
        STABLE(0), POSSIBLE_RISK(1), UNSTABLE(2);

        private final int code;

        DataIntegrity(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
