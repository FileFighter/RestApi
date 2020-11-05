package de.filefighter.rest.domain.health.data;

import lombok.Builder;
import lombok.Getter;

/**
 * This class is a representation of the json model.
 */

@Getter
@Builder
public class SystemHealth {
    private final long uptimeInSeconds;
    private final long userCount;
}
