package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.group.GroupRepository;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDtoService userDtoServiceMock = mock(UserDtoService.class);
    private final GroupRepository groupRepositoryMock = mock(GroupRepository.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private UserBusinessService userBusinessService;

    @BeforeEach
    void setUp() {
        userBusinessService = new UserBusinessService(userRepositoryMock, userDtoServiceMock, groupRepositoryMock, mongoTemplateMock);
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
    void getUserByIdThrowsExceptions() {
        long id = 420;
        when(userRepositoryMock.findByUserId(id)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getUserById(id));
    }

    @Test
    void getUserByIdWorks() {
        long id = 420;
        UserEntity dummyEntity = UserEntity.builder().build();
        User dummyUser = User.builder().build();

        when(userRepositoryMock.findByUserId(id)).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(dummyUser);

        User actual = userBusinessService.getUserById(id);
        assertEquals(dummyUser, actual);
    }

    @Test
    void findUserByUsernameThrowsExceptions() {
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
    void findUserByUsernameWorksCorrectly() {
        String username = "some str ing w i th white spaces";
        UserEntity userEntity = UserEntity.builder().build();
        User user = User.builder().build();

        when(userRepositoryMock.findByLowercaseUsername("somestringwithwhitespaces")).thenReturn(userEntity);
        when(userDtoServiceMock.createDto(userEntity)).thenReturn(user);

        User actual = userBusinessService.findUserByUsername(username);

        assertEquals(user, actual);
    }

    @Test
    void passwordIsValidThrows() {
        String isEmpty = "";
        String[] doNotMatch = new String[]{"pw", "password", "Password", "Password\\", "asdfghjkljasdasda123AS?213+dfghjkfghjkghjk"};

        assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.passwordIsValid(isEmpty));

        for (String string : doNotMatch) {
            assertThrows(UserNotRegisteredException.class, () ->
                    userBusinessService.passwordIsValid(string));
        }
    }

    @Test
    void passwordIsValidWorks() {
        String works = "Password1234?!";
        assertDoesNotThrow(() -> userBusinessService.passwordIsValid(works));
    }

    @Test
    void registerNewUserThrows() {
        String username = "";
        String password = "validPassword1234";
        String confPassword = "validPassword123";
        long[] groups = new long[]{420};

        UserRegisterForm userRegisterForm = UserRegisterForm.builder()
                .username(username)
                .confirmationPassword(confPassword)
                .password(password)
                .groupIds(groups)
                .build();

        //Username not valid.
        assertThrows(RequestDidntMeetFormalRequirementsException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));

        //Username already taken.
        username = "ValidUserName";
        userRegisterForm.setUsername(username);
        when(userRepositoryMock.findByLowercaseUsername(username.toLowerCase())).thenReturn(UserEntity.builder().build());
        when(userDtoServiceMock.createDto(any())).thenReturn(User.builder().build());

        assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));

        //Passwords do not match.
        when(userRepositoryMock.findByLowercaseUsername(username.toLowerCase())).thenReturn(null);
        when(userDtoServiceMock.createDto(any())).thenReturn(null);

        assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));

        // group does not exist
        userRegisterForm.setConfirmationPassword(userRegisterForm.getPassword());
        when(groupRepositoryMock.getGroupById(420)).thenThrow(IllegalArgumentException.class);

        assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
    }

    @Test
    void registerNewUserWorks(){
        String username = "username";
        String password = "validPassword1234";
        String confPassword = "validPassword1234";
        long[] groups = null;

        UserRegisterForm userRegisterForm = UserRegisterForm.builder()
                .username(username)
                .confirmationPassword(confPassword)
                .password(password)
                .groupIds(groups)
                .build();

        assertDoesNotThrow(() -> userBusinessService.registerNewUser(userRegisterForm));
    }
}