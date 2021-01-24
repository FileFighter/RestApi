package de.filefighter.rest.domain.authentication;

import de.filefighter.rest.domain.common.exceptions.InputSanitizerService;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.group.Group;
import org.springframework.stereotype.Service;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;

@Service
public class AuthenticationService implements AuthenticationServiceInterface {

    private final AuthenticationBusinessService authenticationBusinessService;
    private final InputSanitizerService inputSanitizerService;
    private final AccessTokenBusinessService accessTokenBusinessService;

    public AuthenticationService(AuthenticationBusinessService authenticationBusinessService, InputSanitizerService inputSanitizerService, AccessTokenBusinessService accessTokenBusinessService) {
        this.authenticationBusinessService = authenticationBusinessService;
        this.inputSanitizerService = inputSanitizerService;
        this.accessTokenBusinessService = accessTokenBusinessService;
    }

    @Override
    public User basicAuthentication(String base64encodedUsernameAndPasswordWithHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BASIC_PREFIX, base64encodedUsernameAndPasswordWithHeader);
        return authenticationBusinessService.authenticateUserWithUsernameAndPassword(sanitizedHeaderValue);
    }

    @Override
    public User bearerAuthenticationWithAccessToken(String accessTokenWithHeader) {
        String cleanHeader = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenWithHeader);
        String cleanValue = inputSanitizerService.sanitizeTokenValue(cleanHeader);
        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(cleanValue);
        return authenticationBusinessService.authenticateUserWithAccessToken(accessToken);
    }

    @Override
    public User bearerAuthenticationWithRefreshToken(String refreshTokenWithHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, refreshTokenWithHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);
        return authenticationBusinessService.authenticateUserWithRefreshToken(sanitizedTokenString);
    }

    @Override
    public void bearerAuthenticationWithAccessTokenAndGroup(String accessTokenWithHeader, Group group) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenWithHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);
        AccessToken validAccessToken = accessTokenBusinessService.findAccessTokenByValue(sanitizedTokenString);
        authenticationBusinessService.authenticateUserWithAccessTokenAndGroup(validAccessToken, group);
    }
}
