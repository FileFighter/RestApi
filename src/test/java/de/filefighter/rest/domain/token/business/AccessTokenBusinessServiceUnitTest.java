package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccessTokenBusinessServiceUnitTest {

    private final AccessTokenRepository accessTokenRepositoryMock = mock(AccessTokenRepository.class);
    private final AccessTokenDtoService accessTokenDtoServiceMock = mock(AccessTokenDtoService.class);
    private AccessTokenBusinessService accessTokenBusinessService;

    @BeforeEach
    void setUp() {
        accessTokenBusinessService = new AccessTokenBusinessService(accessTokenRepositoryMock, accessTokenDtoServiceMock);
    }

    @Test
    void getValidAccessTokenForUserWhenNoTokenExists() {
        long dummyId = 1234;
        User dummyUser = User.builder().id(dummyId).build();
        AccessToken dummyAccessToken = AccessToken.builder().userId(dummyId).build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity.builder().userId(dummyId).build();

        when(accessTokenRepositoryMock.findByUserId(dummyId)).thenReturn(null);
        when(accessTokenRepositoryMock.save(any())).thenReturn(dummyAccessTokenEntity);
        when(accessTokenDtoServiceMock.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(dummyUser);
        assertEquals(dummyAccessToken, accessToken);
    }

    @Test
    void getValidAccessTokenForUserWhenTokenExists() {
        long dummyId = 1234;
        User dummyUser = User.builder().id(dummyId).build();
        AccessToken dummyAccessToken = AccessToken.builder().userId(dummyId).build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity
                .builder()
                .userId(dummyId)
                .validUntil(Instant.now().getEpochSecond() + AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS + 100)
                .build();

        when(accessTokenRepositoryMock.findByUserId(dummyId)).thenReturn(dummyAccessTokenEntity);
        when(accessTokenDtoServiceMock.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(dummyUser);
        assertEquals(dummyAccessToken, accessToken);
    }

    @Test
    void getValidAccessTokenForUserWhenTokenExistsButIsInvalid() {
        long dummyId = 1234;
        User dummyUser = User.builder().id(dummyId).build();
        AccessToken dummyAccessToken = AccessToken.builder().userId(dummyId).build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity
                .builder()
                .userId(dummyId)
                .validUntil(Instant.now().getEpochSecond())
                .build();

        when(accessTokenRepositoryMock.findByUserId(dummyId)).thenReturn(dummyAccessTokenEntity);
        when(accessTokenRepositoryMock.save(any())).thenReturn(dummyAccessTokenEntity);
        when(accessTokenDtoServiceMock.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(dummyUser);

        assertEquals(dummyAccessToken, accessToken);
        verify(accessTokenRepositoryMock, times(0)).save(dummyAccessTokenEntity); // the newly saved token is different.
    }

    @Test
    void findAccessTokenByValueAndUserIdWithInvalidToken() {
        String tokenValue = "";
        long userId = 1234;

        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                accessTokenBusinessService.findAccessTokenByValueAndUserId(tokenValue, userId)
        );
    }

    @Test
    void findAccessTokenByValueAndUserIdWithTokenNotFound() {
        String tokenValue = "value";
        long userId = 1234;

        when(accessTokenRepositoryMock.findByUserIdAndValue(userId, tokenValue)).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.findAccessTokenByValueAndUserId(tokenValue, userId)
        );
    }

    @Test
    void findAccessTokenByValueAndUserIdWithFoundToken() {
        String tokenValue = "validToken";
        long userId = 1234;
        AccessToken dummyAccessToken = AccessToken.builder().build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity.builder().build();

        when(accessTokenRepositoryMock.findByUserIdAndValue(userId, tokenValue)).thenReturn(dummyAccessTokenEntity);
        when(accessTokenDtoServiceMock.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken actual = accessTokenBusinessService.findAccessTokenByValueAndUserId(tokenValue, userId);

        assertEquals(dummyAccessToken, actual);
    }

    @Test
    void findAccessTokenByValueThrowsException() {
        String invalidFormat = "";
        String validFormat = "ugabuga";

        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                accessTokenBusinessService.findAccessTokenByValue(invalidFormat)
        );

        when(accessTokenRepositoryMock.findByValue(validFormat)).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.findAccessTokenByValue(validFormat)
        );
    }

    @Test
    void findAccessTokenByValueSuccessfully() {
        String validFormat = "ugabuga";

        AccessTokenEntity accessTokenEntity = AccessTokenEntity.builder().build();
        AccessToken expected = AccessToken.builder().build();

        when(accessTokenRepositoryMock.findByValue(validFormat)).thenReturn(accessTokenEntity);
        when(accessTokenDtoServiceMock.createDto(accessTokenEntity)).thenReturn(expected);

        AccessToken actual = accessTokenBusinessService.findAccessTokenByValue(validFormat);

        assertEquals(expected, actual);
    }

    @Test
    void generateRandomTokenValue() {
        String generatedToken = accessTokenBusinessService.generateRandomTokenValue();
        assertEquals(36, generatedToken.length());
    }

    @Test
    void validateAccessTokenValueWithWrongHeader() {
        String header0 = "wrongHeader";
        String header1 = "";
        String header2 = "Bearer: ";

        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                accessTokenBusinessService.validateAccessTokenValue(header0)
        );
        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                accessTokenBusinessService.validateAccessTokenValue(header1)
        );
        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                accessTokenBusinessService.validateAccessTokenValue(header2)
        );
    }

    @Test
    void validateAccessTokenValueButTokenDoesNotExist() {
        String header = "Bearer: something";

        when(accessTokenRepositoryMock.findByValue("something")).thenReturn(null);

        assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.validateAccessTokenValue(header)
        );
    }

    @Test
    void validateAccessTokenValue() {
        String header = "Bearer: something";
        AccessToken expected = AccessToken.builder().build();
        AccessTokenEntity accessTokenEntity = AccessTokenEntity.builder().build();

        when(accessTokenRepositoryMock.findByValue("something")).thenReturn(accessTokenEntity);
        when(accessTokenDtoServiceMock.createDto(accessTokenEntity)).thenReturn(expected);

        AccessToken actual = accessTokenBusinessService.validateAccessTokenValue(header);
        assertEquals(expected, actual);
    }

    @Test
    void getAccessTokenCount(){
        long count = 420;
        when(accessTokenRepositoryMock.count()).thenReturn(count);

        assertEquals(420, accessTokenBusinessService.getAccessTokenCount());
    }
}