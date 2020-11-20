package de.filefighter.rest.domain.user.rest;

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

import static de.filefighter.rest.domain.user.group.Groups.ADMIN;


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
        AccessToken validAccessToken = accessTokenBusinessService.validateAccessTokenValueWithHeader(accessToken);
        userAuthorizationService.authenticateUserWithAccessToken(validAccessToken);
        User user = userBusinessService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPasswordWithHeader) {
        User authenticatedUser = userAuthorizationService.authenticateUserWithUsernameAndPassword(base64encodedUserAndPasswordWithHeader);
        RefreshToken refreshToken = userBusinessService.getRefreshTokenForUser(authenticatedUser);
        return new ResponseEntity<>(refreshToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AccessToken> getAccessTokenByRefreshToken(String refreshTokenWithHeader) {
        User user = userAuthorizationService.authenticateUserWithRefreshToken(refreshTokenWithHeader);
        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(user);
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ServerResponse> updateUserByUserIdAuthenticateWithAccessToken(UserRegisterForm updatedUser, long userId, String accessTokenValue) {
        AccessToken accessToken = accessTokenBusinessService.validateAccessTokenValueWithHeader(accessTokenValue);
        User authenticatedUser = userAuthorizationService.authenticateUserWithAccessToken(accessToken);
        String message = userBusinessService.updateUser(userId, updatedUser, authenticatedUser);
        ServerResponse response = new ServerResponse(HttpStatus.CREATED, message);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ServerResponse> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken) {
        AccessToken validAccessToken = accessTokenBusinessService.validateAccessTokenValueWithHeader(accessToken);
        userAuthorizationService.authenticateUserWithAccessTokenAndGroup(validAccessToken, ADMIN);
        userBusinessService.registerNewUser(newUser);
        return new ResponseEntity<>(new ServerResponse(HttpStatus.CREATED, "User successfully created."), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessToken) {
        AccessToken token = accessTokenBusinessService.validateAccessTokenValueWithHeader(accessToken);
        userAuthorizationService.authenticateUserWithAccessToken(token);
        User foundUser = userBusinessService.findUserByUsername(username);
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }
}
