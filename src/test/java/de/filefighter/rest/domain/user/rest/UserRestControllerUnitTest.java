package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserRestControllerUnitTest {

    private static final UserRestServiceInterface userRestService = mock(UserRestService.class);
    private static UserRestController userRestController;

    @BeforeAll
    static void setUp() {
        userRestController = new UserRestController(userRestService);
    }

    @Test
    void registerNewUser() {
        User user = User.builder().id(420).roles(null).username("kevin").create();
        EntityModel<User> expectedUser = EntityModel.of(user);

        when(userRestService.registerNewUserWithAccessToken(any(), any())).thenReturn(expectedUser);

        EntityModel<User> actualUser = userRestController.registerNewUser("", null);

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void loginUserWithUsernameAndPassword() {
        User user = User.builder().id(420).roles(null).username("kevin").create();
        RefreshToken refreshToken = RefreshToken.builder().refreshToken("token").user(user).build();
        EntityModel<RefreshToken> expectedRefreshToken = EntityModel.of(refreshToken);

        when(userRestService.getRefreshTokenWithUsernameAndPassword(any())).thenReturn(expectedRefreshToken);

        EntityModel<RefreshToken> actualRefreshToken = userRestController.loginUserWithUsernameAndPassword("");

        assertEquals(expectedRefreshToken, actualRefreshToken);
    }

    @Test
    void getAccessTokenAndUserInfoByRefreshTokenAndUserId() {
        AccessToken accessToken = AccessToken.builder().build();
        EntityModel<AccessToken> accessTokenEntityModel = EntityModel.of(accessToken);

        when(userRestService.getAccessTokenByRefreshTokenAndUserId("token", 420)).thenReturn(accessTokenEntityModel);

        EntityModel<AccessToken> actualAccessTokenEntity = userRestController.getAccessTokenAndUserInfoByRefreshTokenAndUserId(420, "token");
        assertEquals(accessTokenEntityModel, actualAccessTokenEntity);
    }

    @Test
    void getUserInfoWithAccessToken() {
        User user = User.builder().id(420).roles(null).username("kevin").create();
        EntityModel<User> expectedUser = EntityModel.of(user);

        when(userRestService.getUserByAccessTokenAndUserId("token", 420)).thenReturn(expectedUser);
        EntityModel<User> actualUser = userRestController.getUserInfoWithAccessToken(420,"token");

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void updateUserWithAccessToken() {
        User user = User.builder().id(420).roles(null).username("kevin").create();
        EntityModel<User> expectedUser = EntityModel.of(user);
        UserRegisterForm userRegisterForm = UserRegisterForm.builder().create();

        when(userRestService.updateUserByAccessTokenAndUserId(userRegisterForm, "token", 420)).thenReturn(expectedUser);
        EntityModel<User> actualUser = userRestController.updateUserWithAccessToken(420,"token", userRegisterForm);

        assertEquals(expectedUser, actualUser);
    }
}