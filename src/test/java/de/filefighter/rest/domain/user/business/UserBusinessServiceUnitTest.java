package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDtoService userDtoServiceMock = mock(UserDtoService.class);
    private UserBusinessService userBusinessService;

    @BeforeEach
    void setUp() {
        userBusinessService = new UserBusinessService(userRepositoryMock, userDtoServiceMock);
    }

    @Test
    void getUserCount() {
        long count = 20;

        when(userRepositoryMock.count()).thenReturn(count);

        long actual = userBusinessService.getUserCount();

        assertEquals(count, actual);
    }

    @Test
    void getUserByUsernameAndPasswordThrowsErrors() {
        String notValid = "";
        String validButDoesntMatch = "something";
        String matchesButIsNotSupportedEncoding = AUTHORIZATION_BASIC_PREFIX + "���";
        String withoutFormalRequirements = AUTHORIZATION_BASIC_PREFIX + "dWdhYnVnYXBhc3N3b3Jk"; //ugabugapassword
        String userNotFound = AUTHORIZATION_BASIC_PREFIX + "dXNlcjpwYXNzd29yZA=="; // user:password

        assertThrows(UserNotAuthenticatedException.class, () ->
                userBusinessService.getUserByUsernameAndPassword(notValid)
        );
        assertThrows(UserNotAuthenticatedException.class, () ->
                userBusinessService.getUserByUsernameAndPassword(validButDoesntMatch)
        );
        assertThrows(RuntimeException.class, () ->
                userBusinessService.getUserByUsernameAndPassword(matchesButIsNotSupportedEncoding)
        );
        assertThrows(UserNotAuthenticatedException.class, () ->
                userBusinessService.getUserByUsernameAndPassword(withoutFormalRequirements)
        );

        when(userRepositoryMock.findByUsernameAndPassword("user", "password")).thenReturn(null);
        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getUserByUsernameAndPassword(userNotFound)
        );
    }

    @Test
    void getUserByUsernameAndPasswordWorksCorrectly() {
        String header = AUTHORIZATION_BASIC_PREFIX + "dXNlcjpwYXNzd29yZA=="; // user:password
        User dummyUser = User.builder().build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByUsernameAndPassword("user", "password")).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = userBusinessService.getUserByUsernameAndPassword(header);
        assertEquals(dummyUser, actual);
    }

    @Test
    void getRefreshTokenForUserWithoutUser() {
        String invalidString = "";
        long userId = 420;
        String username = "someString";

        User dummyUser = User.builder().id(userId).username(username).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getRefreshTokenForUser(dummyUser)
        );
    }

    @Test
    void getRefreshTokenForUserWithInvalidString() {
        String invalidString = "";
        long userId = 420;
        String username = "someString";

        User dummyUser = User.builder().id(userId).username(username).build();
        UserEntity dummyEntity = UserEntity.builder().refreshToken(invalidString).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(dummyEntity);

        assertThrows(IllegalStateException.class, () ->
                userBusinessService.getRefreshTokenForUser(dummyUser)
        );
    }

    @Test
    void getCorrectRefreshTokenForUser() {
        long userId = 420;
        String username = "someString";
        String refreshToken = "someToken";
        User dummyUser = User.builder().id(userId).username(username).build();
        UserEntity dummyEntity = UserEntity.builder().refreshToken(refreshToken).build();
        RefreshToken expected = RefreshToken.builder().refreshToken(refreshToken).user(dummyUser).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(dummyEntity);

        RefreshToken actual = userBusinessService.getRefreshTokenForUser(dummyUser);
        assertEquals(expected, actual);
    }

    // -------------------------------------------------------------------------------------------- //

    @Test
    void getUserByAccessTokenAndUserIdWithInvalidUserId() {
        long userId = 420;
        AccessToken dummyAccessToken = AccessToken.builder().userId(300).build();

        assertThrows(UserNotAuthenticatedException.class, () ->
                userBusinessService.getUserByAccessTokenAndUserId(dummyAccessToken, userId)
        );
    }

    @Test
    void getUserByAccessTokenAndUserIdWithoutUser() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getUserByAccessTokenAndUserId(accessToken, userId)
        );
    }

    @Test
    void getUserByAccessTokenAndUserIdCorrectly() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();
        User dummyUser = User.builder().id(userId).build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = userBusinessService.getUserByAccessTokenAndUserId(accessToken, userId);

        assertEquals(dummyUser, actual);
    }

    // -------------------------------------------------------------------------------------------- //

    @Test
    void getUserByRefreshTokenAndUserIdWithInvalidToken() {
        assertThrows(UserNotAuthenticatedException.class, () ->
                userBusinessService.getUserByRefreshTokenAndUserId("", 0)
        );
    }

    @Test
    void getUserByRefreshTokenAndUserIdWithoutUser() {
        String token = "token";
        long userId = 420;

        when(userRepositoryMock.findByRefreshTokenAndUserId(token, userId)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getUserByRefreshTokenAndUserId(token, userId)
        );
    }

    @Test
    void getUserByRefreshTokenAndUserIdCorrectly() {
        String token = "token";
        long userId = 420;
        User dummyUser = User.builder().id(userId).build();
        UserEntity dummyEntity = UserEntity.builder().refreshToken(token).build();

        when(userRepositoryMock.findByRefreshTokenAndUserId(token, userId)).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = userBusinessService.getUserByRefreshTokenAndUserId(token, userId);

        assertEquals(dummyUser, actual);
    }
}