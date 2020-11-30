package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.business.UserAuthorizationService;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;
import static de.filefighter.rest.domain.user.group.Groups.ADMIN;


@Service
public class UserRestService implements UserRestServiceInterface {

    private final UserBusinessService userBusinessService;
    private final UserAuthorizationService userAuthorizationService;
    private final AccessTokenBusinessService accessTokenBusinessService;
    private final InputSanitizerService inputSanitizerService;

    public UserRestService(UserBusinessService userBusinessService, UserAuthorizationService userAuthorizationService, AccessTokenBusinessService accessTokenBusinessService, InputSanitizerService inputSanitizerService) {
        this.userBusinessService = userBusinessService;
        this.userAuthorizationService = userAuthorizationService;
        this.accessTokenBusinessService = accessTokenBusinessService;
        this.inputSanitizerService = inputSanitizerService;
    }

    @Override
    public ResponseEntity<User> getUserByUserIdAuthenticateWithAccessToken(String accessTokenWithHeader, long userId) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenWithHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);

        AccessToken validAccessToken = accessTokenBusinessService.findAccessTokenByValue(sanitizedTokenString);
        userAuthorizationService.authenticateUserWithAccessToken(validAccessToken);
        User user = userBusinessService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPasswordWithHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BASIC_PREFIX, base64encodedUserAndPasswordWithHeader);

        User authenticatedUser = userAuthorizationService.authenticateUserWithUsernameAndPassword(sanitizedHeaderValue);
        RefreshToken refreshToken = userBusinessService.getRefreshTokenForUser(authenticatedUser);
        return new ResponseEntity<>(refreshToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AccessToken> getAccessTokenByRefreshToken(String refreshTokenWithHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, refreshTokenWithHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);

        User user = userAuthorizationService.authenticateUserWithRefreshToken(sanitizedTokenString);
        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(user);
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ServerResponse> updateUserByUserIdAuthenticateWithAccessToken(UserRegisterForm updatedUser, long userId, String accessTokenHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);

        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(sanitizedTokenString);
        User authenticatedUser = userAuthorizationService.authenticateUserWithAccessToken(accessToken);
        userBusinessService.updateUser(userId, updatedUser, authenticatedUser);
        ServerResponse response = new ServerResponse(HttpStatus.CREATED, "User successfully updated.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ServerResponse> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessTokenHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);

        AccessToken validAccessToken = accessTokenBusinessService.findAccessTokenByValue(sanitizedTokenString);
        userAuthorizationService.authenticateUserWithAccessTokenAndGroup(validAccessToken, ADMIN);
        userBusinessService.registerNewUser(newUser);
        return new ResponseEntity<>(new ServerResponse(HttpStatus.CREATED, "User successfully created."), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessTokenHeader) {
        String sanitizedHeaderValue = inputSanitizerService.sanitizeRequestHeader(AUTHORIZATION_BEARER_PREFIX, accessTokenHeader);
        String sanitizedTokenString = inputSanitizerService.sanitizeTokenValue(sanitizedHeaderValue);
        String sanitizedUserName = InputSanitizerService.sanitizeString(username);

        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValue(sanitizedTokenString);
        userAuthorizationService.authenticateUserWithAccessToken(accessToken);
        User foundUser = userBusinessService.findUserByUsername(sanitizedUserName);
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }
}
