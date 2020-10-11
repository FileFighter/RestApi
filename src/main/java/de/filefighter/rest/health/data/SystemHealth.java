package de.filefighter.rest.health.data;

import lombok.Builder;
import lombok.Getter;

/**
 * This class is a representation of the json model.
 */

@Getter
@Builder(buildMethodName = "create")
public class SystemHealth {
    private final long uptimeInSeconds;
}
