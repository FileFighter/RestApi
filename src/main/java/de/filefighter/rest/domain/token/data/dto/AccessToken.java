package de.filefighter.rest.domain.token.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessToken {
    private String tokenValue;
    @Builder.Default
    private long userId = -1;
    private long validUntil;
}
