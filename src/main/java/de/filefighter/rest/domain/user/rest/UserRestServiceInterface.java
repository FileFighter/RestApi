package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.springframework.hateoas.EntityModel;

public interface UserRestServiceInterface {
    EntityModel<User> getUserByAccessTokenAndUserId(String accessToken, long userId);
    EntityModel<RefreshToken> getRefreshTokenWithUsernameAndPassword(String base64encodedUserAndPassword);
    EntityModel<AccessToken> getAccessTokenByRefreshTokenAndUserId(String refreshToken, long userId);
    EntityModel<User> updateUserByAccessTokenAndUserId(UserRegisterForm updatedUser, String accessToken, long userId);
    EntityModel<User> registerNewUserWithAccessToken(UserRegisterForm newUser, String accessToken);
    EntityModel<User> findUserByUsernameAndAccessToken(String username, String accessToken);
}
