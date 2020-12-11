package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.rest.exceptions.FileFighterDataException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccessTokenBusinessServiceUnitTest {

    private final AccessTokenRepository accessTokenRepositoryMock = mock(AccessTokenRepository.class);
    private final AccessTokenDTOService accessTokenDtoServiceMock = mock(AccessTokenDTOService.class);
    private AccessTokenBusinessService accessTokenBusinessService;

    @BeforeEach
    void setUp() {
        accessTokenBusinessService = new AccessTokenBusinessService(accessTokenRepositoryMock, accessTokenDtoServiceMock);
    }

    @Test
    void getValidAccessTokenForUserWhenNoTokenExists() {
        long dummyId = 1234;
        User dummyUser = User.builder().userId(dummyId).build();
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
        User dummyUser = User.builder().userId(dummyId).build();
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
        User dummyUser = User.builder().userId(dummyId).build();
        AccessToken dummyAccessToken = AccessToken.builder().userId(dummyId).build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity
                .builder()
                .userId(dummyId)
                .validUntil(Instant.now().getEpochSecond())
                .build();

        when(accessTokenRepositoryMock.findByUserId(dummyId)).thenReturn(dummyAccessTokenEntity);
        when(accessTokenRepositoryMock.save(any())).thenReturn(dummyAccessTokenEntity);
        when(accessTokenRepositoryMock.deleteByUserId(dummyId)).thenReturn(1L);
        when(accessTokenDtoServiceMock.createDto(dummyAccessTokenEntity)).thenReturn(dummyAccessToken);

        AccessToken accessToken = accessTokenBusinessService.getValidAccessTokenForUser(dummyUser);

        assertEquals(dummyAccessToken, accessToken);
        verify(accessTokenRepositoryMock, times(0)).save(dummyAccessTokenEntity); // the newly saved token is different.
    }

    @Test
    void getValidAccessTokenForUserWhenTokenDeletionFails() {
        long dummyId = 1234;
        User dummyUser = User.builder().userId(dummyId).build();
        AccessTokenEntity dummyAccessTokenEntity = AccessTokenEntity
                .builder()
                .userId(dummyId)
                .validUntil(Instant.now().getEpochSecond())
                .build();

        when(accessTokenRepositoryMock.findByUserId(dummyId)).thenReturn(dummyAccessTokenEntity);
        when(accessTokenRepositoryMock.deleteByUserId(dummyId)).thenReturn(dummyId - 1);

        FileFighterDataException ex = assertThrows(FileFighterDataException.class, () ->
                accessTokenBusinessService.getValidAccessTokenForUser(dummyUser));
        assertEquals("Internal Error occurred. AccessToken for userId " + dummyId + " could not be deleted.", ex.getMessage());
    }

    @Test
    void findAccessTokenByValueAndUserIdWithTokenNotFound() {
        String tokenValue = "value";
        long userId = 1234;

        when(accessTokenRepositoryMock.findByUserIdAndValue(userId, tokenValue)).thenReturn(null);

        UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.findAccessTokenByValueAndUserId(tokenValue, userId)
        );
        assertEquals("User with the id " + userId + " could not be authenticated.", ex.getMessage());
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
        String validFormat = "ugabuga";

        when(accessTokenRepositoryMock.findByValue(validFormat)).thenReturn(null);

        UserNotAuthenticatedException ex = assertThrows(UserNotAuthenticatedException.class, () ->
                accessTokenBusinessService.findAccessTokenByValue(validFormat)
        );
        assertEquals("User could not be authenticated. AccessToken not found.", ex.getMessage());
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
        String generatedToken = AccessTokenBusinessService.generateRandomTokenValue();
        assertEquals(36, generatedToken.length());
    }

    @Test
    void getAccessTokenCount() {
        long count = 420;
        when(accessTokenRepositoryMock.count()).thenReturn(count);

        assertEquals(420, accessTokenBusinessService.getAccessTokenCount());
    }
}