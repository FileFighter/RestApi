package de.filefighter.rest.health.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(buildMethodName = "create")
public class SystemHealth {
    private long uptimeInSeconds;
}
