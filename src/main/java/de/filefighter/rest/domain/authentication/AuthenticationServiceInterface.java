package de.filefighter.rest.domain.authentication;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Group;

public interface AuthenticationServiceInterface {
    User basicAuthentication(String base64encodedUsernameAndPasswordWithHeader);

    User bearerAuthenticationWithAccessToken(String accessTokenWithHeader);

    User cookieAuthenticationWithAccessToken(String accessToken);

    User bearerAuthenticationWithRefreshToken(String refreshTokenWithHeader);

    void bearerAuthenticationWithAccessTokenAndGroup(String accessTokenWithHeader, Group group);

}
