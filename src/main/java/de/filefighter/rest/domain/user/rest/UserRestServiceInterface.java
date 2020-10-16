package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.RegisterUserForm;
import de.filefighter.rest.domain.user.data.dto.User;
import org.springframework.hateoas.EntityModel;

public interface UserRestServiceInterface {
    EntityModel<User> getUserByAccessTokenAndUserId(String accessToken, long userId);
    EntityModel<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword);
    EntityModel<AccessToken> getAccessTokenByRefreshTokenAndUserId(String refreshToken, long userId);
    EntityModel<User> updateUserByAccessTokenAndUserId(String accessToken, long userId);
    EntityModel<User> registerNewUserWithAccessToken(RegisterUserForm newUser, String accessToken);
}
