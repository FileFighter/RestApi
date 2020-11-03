package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        long expectedCount = 420;

        when(userRepositoryMock.count()).thenReturn(expectedCount);

        long actualCount = userBusinessService.getUserCount();

        assertEquals(expectedCount, actualCount);
    }

    @Test
    void getUserByUsernameAndPassword() {
    }

    @Test
    void getRefreshTokenForUser() {
    }

    @Test
    void getUserByRefreshTokenAndUserId() {
    }

    @Test
    void getUserByAccessTokenAndUserId() {
    }

    @Test
    void getUserEntityByUserIdAndUsername() {
    }
}