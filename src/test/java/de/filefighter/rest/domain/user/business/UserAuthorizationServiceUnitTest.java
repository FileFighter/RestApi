package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.group.Groups;
import org.junit.jupiter.api.Test;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAuthorizationServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDtoService userDtoServiceMock = mock(UserDtoService.class);
    private final UserAuthorizationService userAuthorizationService = new UserAuthorizationService(
            userRepositoryMock,
            userDtoServiceMock);

    @Test
    void authenticateUserWithUsernameAndPasswordThrows() {
        String matchesButIsNotSupportedEncoding = "���"; //funny enough sonar doesnt like this. who cares.
        String matchesButUserWasNotFound = "dXNlcjpwYXNzd29yZA==";

        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                userAuthorizationService.authenticateUserWithUsernameAndPassword(matchesButIsNotSupportedEncoding)
        );

        when(userRepositoryMock.findByLowercaseUsernameAndPassword("user", "password")).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                userAuthorizationService.authenticateUserWithUsernameAndPassword(matchesButUserWasNotFound));
    }

    @Test
    void authenticateUserWithUsernameAndPasswordWorksCorrectly() {
        String usernameAndPassword = "dXNlcjpwYXNzd29yZA=="; // user:password
        User dummyUser = User.builder().build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByLowercaseUsernameAndPassword("user", "password")).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = userAuthorizationService.authenticateUserWithUsernameAndPassword(usernameAndPassword);
        assertEquals(dummyUser, actual);
    }

    @Test
    void authenticateUserWithRefreshTokenThrowsExceptions() {
        String refreshToken = "Something";
        String authString = AUTHORIZATION_BEARER_PREFIX + refreshToken;

        when(userRepositoryMock.findByRefreshToken(refreshToken)).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                userAuthorizationService.authenticateUserWithRefreshToken(authString));
    }

    @Test
    void authenticateUserWithRefreshTokenWorksCorrectly() {
        String refreshToken = "Something";
        UserEntity dummyEntity = UserEntity.builder().build();
        User dummyUser = User.builder().build();

        when(userRepositoryMock.findByRefreshToken(refreshToken)).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actualUser = userAuthorizationService.authenticateUserWithRefreshToken(refreshToken);
        assertEquals(dummyUser, actualUser);
    }


    @Test
    void authenticateUserWithAccessTokenThrows() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                userAuthorizationService.authenticateUserWithAccessToken(accessToken));
    }

    @Test
    void authenticateUserWithAccessTokenWorksCorrectly() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();
        UserEntity userEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntity);

        assertDoesNotThrow(() -> userAuthorizationService.authenticateUserWithAccessToken(accessToken));
    }

    @Test
    void authenticateUserWithAccessTokenAndGroupThrows() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                userAuthorizationService.authenticateUserWithAccessTokenAndGroup(accessToken, Groups.ADMIN));

        when(userRepositoryMock.findByUserId(userId)).thenReturn(UserEntity.builder().groupIds(new long[]{0}).build());

        assertThrows(UserNotAuthenticatedException.class, () ->
                userAuthorizationService.authenticateUserWithAccessTokenAndGroup(accessToken, Groups.ADMIN));
    }

    @Test
    void authenticateUserWithAccessTokenAndGroupWorks() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(UserEntity.builder().groupIds(new long[]{1}).build());

        assertDoesNotThrow(() -> userAuthorizationService.authenticateUserWithAccessTokenAndGroup(accessToken, Groups.ADMIN));
    }
}