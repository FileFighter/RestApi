package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.springframework.http.ResponseEntity;

public interface UserRestServiceInterface {
    ResponseEntity<User> getUserByAccessTokenAndUserId(String accessToken, long userId);
    ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword);
    ResponseEntity<AccessToken> getAccessTokenByRefreshTokenAndUserId(String refreshToken, long userId);
    ResponseEntity<User> updateUserByAccessTokenAndUserId(UserRegisterForm updatedUser, String accessToken, long userId);
    ResponseEntity<User> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken);
    ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessToken);
}
