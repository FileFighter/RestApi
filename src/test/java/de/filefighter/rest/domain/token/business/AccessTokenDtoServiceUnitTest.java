package de.filefighter.rest.domain.token.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenRepository;
import de.filefighter.rest.domain.token.exceptions.AccessTokenNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessTokenDtoServiceUnitTest {

    private final AccessTokenRepository accessTokenRepository = mock(AccessTokenRepository.class);
    private AccessTokenDTOService accessTokenDtoService;

    @BeforeEach
    void setUp() {
        accessTokenDtoService = new AccessTokenDTOService(accessTokenRepository);
    }

    @Test
    void createDto() {
        AccessTokenEntity dummyEntity = AccessTokenEntity.builder()
                .validUntil(420)
                .userId(240)
                .value("token")
                .build();

        AccessToken actual = accessTokenDtoService.createDto(dummyEntity);

        assertEquals(dummyEntity.getUserId(), actual.getUserId());
        assertEquals(dummyEntity.getValidUntil(), actual.getValidUntil());
        assertEquals(dummyEntity.getValue(), actual.getTokenValue());
    }

    @Test
    void findEntityNotSuccessfully() {
        long userId = 420;
        String token = "token";
        AccessToken dummyToken = AccessToken.builder().userId(userId).tokenValue(token).build();

        when(accessTokenRepository.findByUserIdAndValue(userId, token)).thenReturn(null);

        AccessTokenNotFoundException ex = assertThrows(AccessTokenNotFoundException.class, () ->
                accessTokenDtoService.findEntity(dummyToken)
        );
        assertEquals("AccessTokenEntity does not exist for AccessToken with userId "+userId+".", ex.getMessage());
    }

    @Test
    void findEntitySuccessfully() {
        long userId = 420;
        String token = "token";
        AccessToken dummyToken = AccessToken.builder().userId(userId).tokenValue(token).build();
        AccessTokenEntity expected = AccessTokenEntity.builder().userId(userId).value(token).build();

        when(accessTokenRepository.findByUserIdAndValue(userId, token)).thenReturn(expected);

        AccessTokenEntity actual = accessTokenDtoService.findEntity(dummyToken);

        assertEquals(expected, actual);
    }
}