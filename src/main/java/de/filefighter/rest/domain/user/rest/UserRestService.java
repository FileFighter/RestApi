package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;


@Service
public class UserRestService implements UserRestServiceInterface {

    @Override
    public EntityModel<User> getUserByAccessTokenAndUserId(String accessToken, long userId) {
        return null;
    }

    @Override
    public EntityModel<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword) {
        return null;
    }

    @Override
    public EntityModel<AccessToken> getAccessTokenByRefreshTokenAndUserId(String refreshToken, long userId) {
        return null;
    }

    @Override
    public EntityModel<User> updateUserByAccessTokenAndUserId(UserRegisterForm updatedUser, String accessToken, long userId) {
        return null;
    }

    @Override
    public EntityModel<User> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken) {
        return null;
    }
}
