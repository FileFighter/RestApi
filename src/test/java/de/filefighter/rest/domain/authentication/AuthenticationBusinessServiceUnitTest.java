package de.filefighter.rest.domain.authentication;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.business.UserDTOService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.group.Group;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDTOService userDtoServiceMock = mock(UserDTOService.class);
    private final InputSanitizerService inputSanitizerServiceMock = mock(InputSanitizerService.class);
    private final PasswordEncoder passwordEncoderMock = mock(PasswordEncoder.class);
    private final AuthenticationBusinessService authenticationBusinessService =
            new AuthenticationBusinessService(
                    userRepositoryMock,
                    userDtoServiceMock,
                    inputSanitizerServiceMock,
                    passwordEncoderMock);

    @Test
    void authenticateUserWithUsernameAndPasswordThrows() {
        String notSupportedEncoding = "���"; //funny enough sonar doesnt like this. who cares.

        String rawPassword = "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126";
        String username = "KevinDerGroße";
        String base64EncodedUsernameOnly = Base64.getEncoder().encodeToString((username + ":").getBytes(StandardCharsets.UTF_8));
        String base64EncodedUsernameAndPassword = Base64.getEncoder().encodeToString((username + ":" + rawPassword).getBytes(StandardCharsets.UTF_8));

        RequestDidntMeetFormalRequirementsException ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(notSupportedEncoding));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Found unsupported character in header.", ex.getMessage());

        ex = assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(base64EncodedUsernameOnly));
        assertEquals(RequestDidntMeetFormalRequirementsException.getErrorMessagePrefix() + " Credentials didnt meet formal requirements.", ex.getMessage());

        UserNotAuthenticatedException noAuthEx = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(base64EncodedUsernameAndPassword));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " The password didnt match requirenments, please hash the password with SHA-256.", noAuthEx.getMessage());

        when(inputSanitizerServiceMock.passwordIsValid(any())).thenReturn(true);
        when(userRepositoryMock.findByLowercaseUsername(any())).thenReturn(null);

        noAuthEx = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(base64EncodedUsernameAndPassword));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " No User found with this username and password.", noAuthEx.getMessage());

        UserEntity userEntityFound = UserEntity.builder().build();
        when(userRepositoryMock.findByLowercaseUsername(any())).thenReturn(userEntityFound);
        when(passwordEncoderMock.matches(any(), eq(userEntityFound.getPassword()))).thenReturn(false);

        noAuthEx = assertThrows(UserNotAuthenticatedException.class, () ->
                authenticationBusinessService.authenticateUserWithUsernameAndPassword(base64EncodedUsernameAndPassword));
        assertEquals(UserNotAuthenticatedException.getErrorMessagePrefix() + " No User found with this username and password.", noAuthEx.getMessage());
    }

    @Test
    void authenticateUserWithUsernameAndPasswordWorksCorrectly() {
        String username = "user";
        String password = "5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8";
        String usernameAndPassword = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8)); // user:sha256(password)
        User dummyUser = User.builder().build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(inputSanitizerServiceMock.sanitizeString(password)).thenReturn(password);
        when(inputSanitizerServiceMock.sanitizeString(username)).thenReturn(username);
        when(inputSanitizerServiceMock.passwordIsValid(password)).thenReturn(true);
        when(userRepositoryMock.findByLowercaseUsername(username)).thenReturn(dummyEntity);
        when(passwordEncoderMock.matches(password, null)).thenReturn(true);
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