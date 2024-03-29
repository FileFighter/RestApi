package de.filefighter.rest.domain.authentication;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.Pair;
import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.group.Group;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;

@Service
@Log4j2
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
    public User cookieAuthenticationWithAccessToken(String accessTokenFromCookie) {
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(accessTokenFromCookie);
        AccessToken validAccessToken = accessTokenBusinessService.findAccessTokenByValue(sanitizedTokenString);
        return authenticationBusinessService.authenticateUserWithAccessToken(validAccessToken);
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

    @Override
    public User authenticateUserWithCookieOrHeader(Pair<String, String> accessTokenValueOrHeader) {
        String tokenFromCookie = accessTokenValueOrHeader.getFirst();
        String tokenFromHeader = accessTokenValueOrHeader.getSecond();

        User authenticatedUser = null;
        try {
            authenticatedUser = this.bearerAuthenticationWithAccessToken(tokenFromHeader);
        } catch (RequestDidntMeetFormalRequirementsException ex) {
            log.debug("Header {} was not valid. Trying cookies next...", tokenFromHeader);

            try {
                authenticatedUser = this.cookieAuthenticationWithAccessToken(tokenFromCookie);
            } catch (RequestDidntMeetFormalRequirementsException exception) {
                log.debug("Cookie {} was also not valid. Throwing Exception...", tokenFromCookie);
                throw new UserNotAuthenticatedException("No user found with this authentication.");
            }
        }
        return authenticatedUser;
    }
}
