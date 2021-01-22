package de.filefighter.rest.domain.authentication;

import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.business.UserDTOService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.group.Group;
import org.junit.jupiter.api.Test;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDTOService userDtoServiceMock = mock(UserDTOService.class);
    private final AuthenticationBusinessService authenticationBusinessService = new AuthenticationBusinessService(
            userRepositoryMock,
            userDtoServiceMock);

    @Test
    void authenticateUserWithUsernameAndPasswordThrows() {
        String matchesButIsNotSupportedEncoding = "���"; //funny enough sonar doesnt like this. who cares.
        String matchesButUserWasNotFound = "dXNlcjpwYXNzd29yZA==";
        String onlyContainsUsername = "dXNlcm5hbWU=";

        RuntimeException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(matchesButIsNotSupportedEncoding));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Found unsupported character in header.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(onlyContainsUsername));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Credentials didnt meet formal requirements.", ex.getMessage());

        when(userRepositoryMock.findByLowercaseUsernameAndPassword("user", "password")).thenReturn(null);

        ex = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(matchesButUserWasNotFound));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " No User found with this username and password.", ex.getMessage());
    }

    @Test
    void authenticateUserWithUsernameAndPasswordWorksCorrectly() {
        String usernameAndPassword = "dXNlcjpwYXNzd29yZA=="; // user:password
        User dummyUser = User.builder().build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByLowercaseUsernameAndPassword("user", "password")).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = authenticationBusinessService.authenticateUserWithUsernameAndPassword(usernameAndPassword);
        assertEquals(dummyUser, actual);
    }

    @Test
    void authenticateUserWithRefreshTokenThrowsExceptions() {
        String refreshToken = "Something";
        String authString = AUTHORIZATION_BEARER_PREFIX + refreshToken;

        when(userRepositoryMock.findByRefreshToken(refreshToken)).thenReturn(null);

        UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithRefreshToken(authString));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " No user found for this Refresh Token.", ex.getMessage());
    }

    @Test
    void authenticateUserWithRefreshTokenWorksCorrectly() {
        String refreshToken = "Something";
        UserEntity dummyEntity = UserEntity.builder().build();
        User dummyUser = User.builder().build();

        when(userRepositoryMock.findByRefreshToken(refreshToken)).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actualUser = authenticationBusinessService.authenticateUserWithRefreshToken(refreshToken);
        assertEquals(dummyUser, actualUser);
    }


    @Test
    void authenticateUserWithAccessTokenThrows() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(null);

        UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithAccessToken(accessToken));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " UserId was " + userId, ex.getMessage());
    }

    @Test
    void authenticateUserWithAccessTokenWorksCorrectly() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();
        UserEntity userEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntity);

        assertDoesNotThrow(() -> authenticationBusinessService.authenticateUserWithAccessToken(accessToken));
    }

    @Test
    void authenticateUserWithAccessTokenAndGroupThrows() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(null);

        UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithAccessTokenAndGroup(accessToken, Group.ADMIN));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " UserId was " + userId, ex.getMessage());

        when(userRepositoryMock.findByUserId(userId)).thenReturn(UserEntity.builder().groupIds(new long[]{0}).build());

        ex = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithAccessTokenAndGroup(accessToken, Group.ADMIN));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " Not in necessary group.", ex.getMessage());
    }

    @Test
    void authenticateUserWithAccessTokenAndGroupWorks() {
        long userId = 420;
        AccessToken accessToken = AccessToken.builder().userId(userId).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(UserEntity.builder().groupIds(new long[]{1}).build());

        assertDoesNotThrow(() -> authenticationBusinessService.authenticateUserWithAccessTokenAndGroup(accessToken, Group.ADMIN));
    }
}