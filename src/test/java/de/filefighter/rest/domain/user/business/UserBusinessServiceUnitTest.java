package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDtoService userDtoServiceMock = mock(UserDtoService.class);
    private UserBusinessService userBusinessService;

    @BeforeEach
    void setUp() {
        userBusinessService = new UserBusinessService(userRepositoryMock, userDtoServiceMock, groupRepository);
    }

    @Test
    void getUserCount() {
        long count = 20;

        when(userRepositoryMock.count()).thenReturn(count);

        long actual = userBusinessService.getUserCount();

        assertEquals(count, actual);
    }

    @Test
    void getRefreshTokenForUserWithoutUser() {
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

    @Test
    void getUserByIdThrowsExceptions(){
        long id = 420;
        when(userRepositoryMock.findByUserId(id)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getUserById(id));
    }

    @Test
    void getUserByIdWorks(){
        long id = 420;
        UserEntity dummyEntity = UserEntity.builder().build();
        User dummyUser = User.builder().build();

        when(userRepositoryMock.findByUserId(id)).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = userBusinessService.getUserById(id);
        assertEquals(dummyUser, actual);
    }

    @Test
    void findUserByUsernameThrowsExceptions(){
        String invalidFormat = "";
        String validFormat = "ugabuga";

        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                userBusinessService.findUserByUsername(invalidFormat)
        );

        when(userRepositoryMock.findByLowercaseUsername(validFormat)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.findUserByUsername(validFormat)
        );
    }

    @Test
    void findUserByUsernameWorksCorrectly(){
        String username = "some str ing w i th white spaces";
        UserEntity userEntity = UserEntity.builder().build();
        User user = User.builder().build();

        when(userRepositoryMock.findByLowercaseUsername("somestringwithwhitespaces")).thenReturn(userEntity);
        when(userDtoServiceMock.createDto(userEntity)).thenReturn(user);

        User actual = userBusinessService.findUserByUsername(username);

        assertEquals(user, actual);
    }
}