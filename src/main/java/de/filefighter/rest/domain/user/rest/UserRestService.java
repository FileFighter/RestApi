package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public class UserRestService implements UserRestServiceInterface {

    @Override
    public ResponseEntity<User> getUserByAccessTokenAndUserId(String accessToken, long userId) {
        return null;
    }

    @Override
    public ResponseEntity<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword) {
        return null;
    }

    @Override
    public ResponseEntity<AccessToken> getAccessTokenByRefreshTokenAndUserId(String refreshToken, long userId) {
        return null;
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
        return null;
    }
}
