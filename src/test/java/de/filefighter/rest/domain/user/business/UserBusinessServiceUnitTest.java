package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.exceptions.UserNotUpdatedException;
import de.filefighter.rest.domain.user.group.GroupRepository;
import de.filefighter.rest.domain.user.group.Groups;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDTOService userDtoServiceMock = mock(UserDTOService.class);
    private final GroupRepository groupRepositoryMock = mock(GroupRepository.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private UserBusinessService userBusinessService;

    private static UserEntity userEntityMock;

    @BeforeEach
    void setUp() {
        userBusinessService = new UserBusinessService(userRepositoryMock, userDtoServiceMock, groupRepositoryMock, mongoTemplateMock);
        userEntityMock = UserEntity.builder()
                .lowercaseUsername("username")
                .userId(420)
                .username("Username")
                .password("password")
                .refreshToken("refreshToken")
                .groupIds(new long[0])
                .build();
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

        User dummyUser = User.builder().userId(userId).username(username).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(null);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getRefreshTokenForUser(dummyUser)
        );
        assertEquals("Could not find user with userId " + userId + ".", ex.getMessage());
    }

    @Test
    void getRefreshTokenForUserWithInvalidString() {
        String invalidString = "";
        long userId = 420;
        String username = "someString";

        User dummyUser = User.builder().userId(userId).username(username).build();
        UserEntity dummyEntity = UserEntity.builder().refreshToken(invalidString).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(dummyEntity);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                userBusinessService.getRefreshTokenForUser(dummyUser)
        );
        assertEquals("RefreshToken was invalid or empty in db.", ex.getMessage());

    }

    @Test
    void getCorrectRefreshTokenForUser() {
        long userId = 420;
        String username = "someString";
        String refreshToken = "someToken";
        User dummyUser = User.builder().userId(userId).username(username).build();
        UserEntity dummyEntity = UserEntity.builder().refreshToken(refreshToken).build();
        RefreshToken expected = RefreshToken.builder().tokenValue(refreshToken).user(dummyUser).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(dummyEntity);

        RefreshToken actual = userBusinessService.getRefreshTokenForUser(dummyUser);
        assertEquals(expected, actual);
    }

    @Test
    void getUserByIdThrowsExceptions() {
        long id = 420;
        when(userRepositoryMock.findByUserId(id)).thenReturn(null);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                userBusinessService.getUserById(id));

        assertEquals("Could not find user with userId " + id + ".", ex.getMessage());
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
        String validFormat = "ugabuga";

        when(userRepositoryMock.findByLowercaseUsername(validFormat)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userBusinessService.findUserByUsername(validFormat)
        );
        assertEquals("User with username '" + validFormat + "' not found.", exception.getMessage());
    }

    @Test
    void findUserByUsernameWorksCorrectly() {
        String username = "someusername";
        UserEntity userEntity = UserEntity.builder().build();
        User user = User.builder().build();

        when(userRepositoryMock.findByLowercaseUsername(username)).thenReturn(userEntity);
        when(userDtoServiceMock.createDto(userEntity)).thenReturn(user);

        User actual = userBusinessService.findUserByUsername(username);

        assertEquals(user, actual);
    }

    @Test
    void passwordIsValidReturnsFalse() {
        String isEmpty = "";
        String[] doNotMatch = new String[]{"pw", "password", "Password", "Password\\", "asdfghjkljasdasda123AS?213+dfghjkfghjkghjk"};

        boolean actualState = userBusinessService.passwordIsValid(isEmpty);

        for (String string : doNotMatch) {
            assertFalse(userBusinessService.passwordIsValid(string));
        }

        assertFalse(actualState);
    }

    @Test
    void passwordIsValidWorks() {
        String works = "Password1234?!";
        assertDoesNotThrow(() -> userBusinessService.passwordIsValid(works));

        userBusinessService.passwordCheckDisabled = true;  //remember to reset it if beforeEach is removed.
        String doesWork = "pw";
        assertDoesNotThrow(() -> userBusinessService.passwordIsValid(doesWork));
    }

    @Test
    void registerNewUserThrows() {
        String notValidUsername = null;
        String username = "ValidUserName";
        String notValidPassword = "password";
        String password = "validPassword1234";
        String confPassword = "validPassword123";
        long[] groups = new long[]{420};

        UserRegisterForm userRegisterForm = UserRegisterForm.builder()
                .username(username)
                .confirmationPassword(confPassword)
                .password(password)
                .groupIds(groups)
                .build();

        // not valid.
        userRegisterForm.setUsername(notValidUsername);
        UserNotRegisteredException ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Username was not valid.", ex.getMessage());

        // username taken
        userRegisterForm.setUsername(username);
        when(userRepositoryMock.findByLowercaseUsername(username.toLowerCase())).thenReturn(UserEntity.builder().build());
        when(userDtoServiceMock.createDto(any())).thenReturn(User.builder().build());

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Username already taken.", ex.getMessage());

        //passwords empty
        when(userRepositoryMock.findByLowercaseUsername(username.toLowerCase())).thenReturn(null);
        when(userDtoServiceMock.createDto(any())).thenReturn(null);
        userRegisterForm.setPassword("");
        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Wanted to change password, but password was not valid.", ex.getMessage());

        userRegisterForm.setPassword("somepassword");
        userRegisterForm.setConfirmationPassword("");
        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Wanted to change password, but password was not valid.", ex.getMessage());

        //Passwords not valid
        userRegisterForm.setConfirmationPassword(notValidPassword);
        userRegisterForm.setPassword(notValidPassword);
        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.", ex.getMessage());

        //Passwords do not match.
        userRegisterForm.setPassword(password);

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Passwords do not match.", ex.getMessage());

        //Username exists in password.
        userRegisterForm.setUsername("Password123");
        userRegisterForm.setConfirmationPassword(userRegisterForm.getPassword());

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. Username must not appear in password.", ex.getMessage());

        // group does not exist
        userRegisterForm.setUsername(username);
        userRegisterForm.setConfirmationPassword(userRegisterForm.getPassword());
        when(groupRepositoryMock.getGroupById(420)).thenThrow(IllegalArgumentException.class);

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals("User could not be registered. One or more groups do not exist.", ex.getMessage());
    }

    @Test
    void registerNewUserWorks() {
        String username = "username";
        String password = "validPassword1234";
        String confPassword = "validPassword1234";
        long[] groups = new long[]{0};

        UserRegisterForm userRegisterForm = UserRegisterForm.builder()
                .username(username)
                .confirmationPassword(confPassword)
                .password(password)
                .groupIds(groups)
                .build();

        assertDoesNotThrow(() -> userBusinessService.registerNewUser(userRegisterForm));

        userRegisterForm.setGroupIds(null);
        assertDoesNotThrow(() -> userBusinessService.registerNewUser(userRegisterForm));
    }

    @Test
    void updateUserThrows() {
        final UserRegisterForm userRegisterForm = null;
        long userId = 420;
        User authenticatedUser = User.builder().build();

        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. No updates specified.", ex.getMessage());

        UserRegisterForm userRegisterForm1 = UserRegisterForm.builder().build();
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals("User could not get updated. Authenticated User is not allowed.", ex.getMessage());

        authenticatedUser.setGroups(new Groups[]{Groups.UNDEFINED});
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals("User could not get updated. Only Admins are allowed to update other users.", ex.getMessage());

        //user not found with id.
        authenticatedUser.setGroups(new Groups[]{Groups.ADMIN});
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals("User could not get updated. User does not exist, use register endpoint.", ex.getMessage());

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals("User could not get updated. No changes were made.", ex.getMessage());
    }

    @Test
    void updateUserNameThrows() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Groups[]{Groups.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);

        userRegisterForm.setUsername("");
        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. Wanted to change username, but username was not valid.", ex.getMessage());

        String validUserName = "ValidUserNameButExists.";
        userRegisterForm.setUsername(validUserName);
        when(userRepositoryMock.findByLowercaseUsername(validUserName.toLowerCase())).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(User.builder().build());
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. Username already taken.", ex.getMessage());
    }

    @Test
    void updateUserNameWorks() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().username("newUserName").build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Groups[]{Groups.FAMILY}).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);

        assertDoesNotThrow(() -> userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
    }

    @Test
    void updatePasswordThrows() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Groups[]{Groups.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("password").build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);

        userRegisterForm.setPassword("");
        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. Wanted to change password, but password was not valid.", ex.getMessage());

        userRegisterForm.setPassword("somepw");
        userRegisterForm.setConfirmationPassword("");
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. Wanted to change password, but password was not valid.", ex.getMessage());

        userRegisterForm.setPassword("somepw");
        userRegisterForm.setConfirmationPassword("somepw");
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser), "Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.");
        assertEquals("User could not get updated. Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.", ex.getMessage());

        userRegisterForm.setPassword("Somepw12345");
        userRegisterForm.setConfirmationPassword("Somepw1234");
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser), "Passwords do not match.");
        assertEquals("User could not get updated. Passwords do not match.", ex.getMessage());

        String validPassword = "ValidPassword1234!=";
        userRegisterForm.setPassword(validPassword);
        userRegisterForm.setConfirmationPassword(validPassword);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser), "Username must not appear in password.");
        assertEquals("User could not get updated. Username must not appear in password.", ex.getMessage());
    }

    @Test
    void updatePasswordWorks() {
        String password = "validPassword1234";
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().password(password).confirmationPassword(password).build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Groups[]{Groups.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("UGABUGA").build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        assertDoesNotThrow(() -> userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
    }

    @Test
    void updateGroupsThrows() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Groups[]{Groups.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("password").build();

        long[] groups = new long[]{0};
        userRegisterForm.setGroupIds(groups);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(groupRepositoryMock.getGroupsByIds(groups)).thenReturn(new Groups[]{Groups.ADMIN});
        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. Only admins can add users to group Admin.", ex.getMessage());

        groups = new long[]{123032, 1230213};
        userRegisterForm.setGroupIds(groups);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(groupRepositoryMock.getGroupsByIds(groups)).thenThrow(new IllegalArgumentException("id doesnt belong to a group"));
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals("User could not get updated. One or more groups do not exist.", ex.getMessage());
    }

    @Test
    void updateGroupsWorks() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Groups[]{Groups.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("password").build();

        long[] groups = new long[]{0};
        userRegisterForm.setGroupIds(groups);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(groupRepositoryMock.getGroupsByIds(groups)).thenReturn(new Groups[]{Groups.FAMILY});
        assertDoesNotThrow(() -> userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
    }

    @Test
    void generateRandomUserIdWorks() {
        long actualValue = userBusinessService.generateRandomUserId();
        assertTrue(0 <= actualValue && actualValue <= UserBusinessService.USER_ID_MAX);
    }
}