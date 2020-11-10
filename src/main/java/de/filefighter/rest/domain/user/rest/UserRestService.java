package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.business.UserBusinessService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class UserRestService implements UserRestServiceInterface {

    private final UserBusinessService userBusinessService;
    private final AccessTokenBusinessService accessTokenBusinessService;

    public UserRestService(UserBusinessService userBusinessService, AccessTokenBusinessService accessTokenBusinessService) {
        this.userBusinessService = userBusinessService;
        this.accessTokenBusinessService = accessTokenBusinessService;
    }

    @Override
    public ResponseEntity<User> getUserByAccessTokenAndUserId(String accessTokenValue, long userId) {
        String cleanValue = accessTokenBusinessService.checkBearerHeader(accessTokenValue);
        AccessToken accessToken = accessTokenBusinessService.findAccessTokenByValueAndUserId(cleanValue, userId);
        User user = userBusinessService.getUserByAccessTokenAndUserId(accessToken, userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword) {
        User user = userBusinessService.getUserByUsernameAndPassword(base64encodedUserAndPassword);
        RefreshToken refreshToken = userBusinessService.getRefreshTokenForUser(user);
        return new ResponseEntity<>(refreshToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<AccessToken> getAccessTokenByRefreshTokenAndUserId(String refreshToken, long userId) {
        String cleanValue = accessTokenBusinessService.checkBearerHeader(refreshToken);
        User user = userBusinessService.getUserByRefreshTokenAndUserId(cleanValue, userId);
        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(user);
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<User> updateUserByAccessTokenAndUserId(UserRegisterForm updatedUser, String accessToken, long userId) {
        return null;
    }

    @Override
    public ResponseEntity<User> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken) {
        return null;
    }

    @Override
    public ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessToken) {
        String cleanValue = accessTokenBusinessService.checkBearerHeader(accessToken);
        AccessToken token = accessTokenBusinessService.findAccessTokenByValue(cleanValue);
        User foundUser = userBusinessService.findUserByUsername(username);
        return new ResponseEntity<>(foundUser, HttpStatus.OK);
    }
}
