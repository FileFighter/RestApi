package de.filefighter.rest.domain.token.data.dto;

import de.filefighter.rest.domain.user.data.dto.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshToken {
    private final String refreshToken;
    private final User user;
}
