package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.authentication.AuthenticationService;
import de.filefighter.rest.domain.common.exceptions.InputSanitizerService;
import de.filefighter.rest.domain.filesystem.business.FileSystemHelperService;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static de.filefighter.rest.domain.user.group.Group.ADMIN;


@Service
public class UserRestService implements UserRestServiceInterface {

    private final UserBusinessService userBusinessService;
    private final AccessTokenBusinessService accessTokenBusinessService;
    private final FileSystemHelperService fileSystemHelperService;
    private final AuthenticationService authenticationService;

    public UserRestService(UserBusinessService userBusinessService, AccessTokenBusinessService accessTokenBusinessService, FileSystemHelperService fileSystemHelperService, AuthenticationService authenticationService) {
        this.userBusinessService = userBusinessService;
        this.accessTokenBusinessService = accessTokenBusinessService;
        this.fileSystemHelperService = fileSystemHelperService;
        this.authenticationService = authenticationService;
    }

    @Override
    public ResponseEntity<User> getUserByUserIdAuthenticateWithAccessToken(String accessTokenWithHeader, long userId) {
        authenticationService.bearerAuthenticationWithAccessToken(accessTokenWithHeader);
        User user = userBusinessService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPasswordWithHeader) {
        User authenticatedUser = authenticationService.basicAuthentication(base64encodedUserAndPasswordWithHeader);
        RefreshToken refreshToken = userBusinessService.getRefreshTokenForUser(authenticatedUser);
        return new ResponseEntity<>(refreshToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AccessToken> getAccessTokenByRefreshToken(String refreshTokenWithHeader) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithRefreshToken(refreshTokenWithHeader);
        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(authenticatedUser);
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ServerResponse> updateUserByUserIdAuthenticateWithAccessToken(UserRegisterForm updatedUser, long userId, String accessTokenHeader) {
        User authenticatedUser = authenticationService.bearerAuthenticationWithAccessToken(accessTokenHeader);
        userBusinessService.updateUser(userId, updatedUser, authenticatedUser);
        ServerResponse response = new ServerResponse(HttpStatus.CREATED, "User successfully updated.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ServerResponse> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessTokenHeader) {
        authenticationService.bearerAuthenticationWithAccessTokenAndGroup(accessTokenHeader, ADMIN);
        UserEntity registeredUserEntity = userBusinessService.registerNewUser(newUser);
        fileSystemHelperService.createBasicFilesForNewUser(registeredUserEntity);
        return new ResponseEntity<>(new ServerResponse(HttpStatus.CREATED, "User successfully created."), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessTokenHeader) {
        String sanitizedUserName = InputSanitizerService.sanitizeString(username);
        authenticationService.bearerAuthenticationWithAccessToken(accessTokenHeader);
        User foundUser = userBusinessService.findUserByUsername(sanitizedUserName);
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }
}
