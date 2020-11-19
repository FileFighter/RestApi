package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.rest.ServerResponse;
import org.springframework.http.ResponseEntity;

public interface UserRestServiceInterface {
    ResponseEntity<User> getUserByUserIdAuthenticateWithAccessToken(String accessToken, long userId);
    ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword);
    ResponseEntity<AccessToken> getAccessTokenByRefreshToken(String refreshToken);
    ResponseEntity<User> updateUserWithAccessToken(UserRegisterForm updatedUser, String accessToken);
    ResponseEntity<ServerResponse> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken);
    ResponseEntity<User> findUserByUsernameAndAccessToken(String username, String accessToken);
}
