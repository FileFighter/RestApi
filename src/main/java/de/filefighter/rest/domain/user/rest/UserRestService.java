package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.configuration.RestConfiguration;
import de.filefighter.rest.domain.common.Utils;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.business.UserAuthorizationService;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;


@Service
public class UserRestService implements UserRestServiceInterface {

    private final UserBusinessService userBusinessService;
    private final UserAuthorizationService userAuthorizationService;
    private final AccessTokenBusinessService accessTokenBusinessService;

    public UserRestService(UserBusinessService userBusinessService, UserAuthorizationService userAuthorizationService, AccessTokenBusinessService accessTokenBusinessService) {
        this.userBusinessService = userBusinessService;
        this.userAuthorizationService = userAuthorizationService;
        this.accessTokenBusinessService = accessTokenBusinessService;
    }

    @Override
    public ResponseEntity<User> getUserByUserIdAuthenticateWithAccessToken(String accessToken, long userId) {
        AccessToken validAccessToken = accessTokenBusinessService.validateAccessTokenValue(accessToken);
        userAuthorizationService.authenticateUserWithAccessToken(validAccessToken);
        User user = userBusinessService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword) {
        User authenticatedUser = userAuthorizationService.authenticateUserWithUsernameAndPassword(base64encodedUserAndPassword);
        RefreshToken refreshToken = userBusinessService.getRefreshTokenForUser(authenticatedUser);
        return new ResponseEntity<>(refreshToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AccessToken> getAccessTokenByRefreshToken(String refreshToken) {
        String cleanValue = Utils.validateAuthorizationHeader(AUTHORIZATION_BEARER_PREFIX, refreshToken);
        User user = userAuthorizationService.authenticateUserWithRefreshToken(cleanValue);
        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(user);
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> updateUserWithAccessToken(UserRegisterForm updatedUser, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<User> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessToken) {
        AccessToken token = accessTokenBusinessService.validateAccessTokenValue(accessToken);
        userAuthorizationService.authenticateUserWithAccessToken(token);
        User foundUser = userBusinessService.findUserByUsername(username);
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }
}
