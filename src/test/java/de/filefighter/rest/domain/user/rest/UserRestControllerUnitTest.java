package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.rest.ServerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

class UserRestControllerUnitTest {

    private final UserRestServiceInterface userRestServiceMock = mock(UserRestService.class);
    private UserRestController userRestController;

    @BeforeEach
    void setUp() {
        userRestController = new UserRestController(userRestServiceMock);
    }

    @Test
    void registerNewUser() {
        ServerResponse expected = new ServerResponse(OK,"");
        ResponseEntity<ServerResponse> expectedEntity = new ResponseEntity<>(expected, CREATED);

        when(userRestServiceMock.registerNewUserWithAccessToken(any(), any())).thenReturn(expectedEntity);

        ResponseEntity<ServerResponse> actual = userRestController.registerNewUser("", null);

        assertEquals(expectedEntity, actual);
    }

    @Test
    void loginUserWithUsernameAndPassword() {
        User user = User.builder().userId(420).groups(null).username("kevin").build();
        RefreshToken refreshToken = RefreshToken.builder().tokenValue("token").user(user).build();
        ResponseEntity<RefreshToken> expectedRefreshToken = new ResponseEntity<>(refreshToken, OK);

        when(userRestServiceMock.getRefreshTokenWithUsernameAndPassword(any())).thenReturn(expectedRefreshToken);

        ResponseEntity<RefreshToken> actualRefreshToken = userRestController.loginWithUsernameAndPassword("");

        assertEquals(expectedRefreshToken, actualRefreshToken);
    }

    @Test
    void getAccessTokenAndUserInfoByRefreshTokenAndUserId() {
        AccessToken accessToken = AccessToken.builder().build();
        ResponseEntity<AccessToken> accessTokenEntityModel = new ResponseEntity<>(accessToken, OK);

        when(userRestServiceMock.getAccessTokenByRefreshToken("token")).thenReturn(accessTokenEntityModel);

        ResponseEntity<AccessToken> actualAccessTokenEntity = userRestController.getAccessToken("token");
        assertEquals(accessTokenEntityModel, actualAccessTokenEntity);
    }

    @Test
    void getUserInfoWithAccessToken() {
        User user = User.builder().userId(420).groups(null).username("kevin").build();
        ResponseEntity<User> expectedUser = new ResponseEntity<>(user, OK);

        when(userRestServiceMock.getUserByUserIdAuthenticateWithAccessToken("token", 420)).thenReturn(expectedUser);
        ResponseEntity<User> actualUser = userRestController.getUserInfo(420,"token");

        assertEquals(expectedUser, actualUser);
    }

    @Test
    void updateUserWithAccessToken() {
        ResponseEntity<ServerResponse> expectedResponse = new ResponseEntity<>(new ServerResponse(CREATED, "uga"), CREATED);
        UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();

        when(userRestServiceMock.updateUserByUserIdAuthenticateWithAccessToken(userRegisterForm, 0, "token")).thenReturn(expectedResponse);
        ResponseEntity<ServerResponse> actualResponse = userRestController.updateUser(0, "token", userRegisterForm);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void findUserByUsername(){
        User user = User.builder().userId(420).groups(null).username("kevin").build();
        ResponseEntity<User> expectedUser = new ResponseEntity<>(user, OK);

        String username="kevin";
        String accessToken="token";
        when(userRestServiceMock.findUserByUsernameAndAccessToken(username, accessToken)).thenReturn(expectedUser);

        ResponseEntity<User> actualUser = userRestController.findUserByUsername(accessToken, username);
        assertEquals(expectedUser, actualUser);
    }
}