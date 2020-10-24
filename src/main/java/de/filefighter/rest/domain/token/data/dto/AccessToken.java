package de.filefighter.rest.domain.token.data.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessToken {
    private String token;
    private long userId;
    private long validUntil;
}
