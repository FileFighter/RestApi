package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccessTokenBusinessServiceUnitTest {

    private final AccessTokenRepository accessTokenRepositoryMock = mock(AccessTokenRepository.class);
    private final AccessTokenDtoService accessTokenDtoService = mock(AccessTokenDtoService.class);
    private AccessTokenBusinessService accessTokenBusinessService;

    @BeforeEach
    void setUp() {
        accessTokenBusinessService = new AccessTokenBusinessService(accessTokenRepositoryMock, accessTokenDtoService);
    }

    @Test
    void getValidAccessTokenForUserWhenNoTokenExists() {
        long dummyId = 1234;
        User dummyUser = User.builder().id(dummyId).build();
        AccessToken dummyAccessToken = AccessToken.builder().userId(dummyId).build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity.builder().userId(dummyId).build();

        when(accessTokenRepositoryMock.findByUserId(dummyId)).thenReturn(null);
        when(accessTokenRepositoryMock.save(any())).thenReturn(dummyAccessTokenEntity);
        when(accessTokenDtoService.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

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
        when(accessTokenDtoService.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

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
        when(accessTokenDtoService.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(dummyUser);

        assertEquals(dummyAccessToken, accessToken);
        verify(accessTokenRepositoryMock, times(0)).save(dummyAccessTokenEntity); // the newly saved token is different.
    }

    @Test
    void findAccessTokenByValueAndUserIdWithInvalidToken() {
        String tokenValue = "";
        long userId = 1234;

        assertThrows(IllegalArgumentException.class, () ->
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
        when(accessTokenDtoService.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken actual = accessTokenBusinessService.findAccessTokenByValueAndUserId(tokenValue, userId);

        assertEquals(dummyAccessToken, actual);
    }

    @Test
    void generateRandomTokenValue() {
        String generatedToken = accessTokenBusinessService.generateRandomTokenValue();
        assertEquals(36, generatedToken.length());
    }

    @Test
    void checkBearerHeaderWithWrongHeader(){
        String header0 = "wrongHeader";
        String header1 = "";
        String header2 = "Bearer: ";

        assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.checkBearerHeader(header0)
        );
        assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.checkBearerHeader(header1)
        );
        assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.checkBearerHeader(header2)
        );
    }

    @Test
    void checkBearerHeaderWithCorrectHeader(){
        String header = "Bearer: something";
        String expected = "something";

        String actual = accessTokenBusinessService.checkBearerHeader(header);

        assertEquals(expected, actual);
    }
}