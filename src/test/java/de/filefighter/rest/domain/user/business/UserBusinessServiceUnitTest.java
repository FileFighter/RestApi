package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.exceptions.UserNotUpdatedException;
import de.filefighter.rest.domain.user.group.Group;
import de.filefighter.rest.domain.user.group.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserBusinessServiceUnitTest {

    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private final UserDTOService userDtoServiceMock = mock(UserDTOService.class);
    private final GroupRepository groupRepositoryMock = mock(GroupRepository.class);
    private final MongoTemplate mongoTemplateMock = mock(MongoTemplate.class);
    private final InputSanitizerService inputSanitizerServiceMock = mock(InputSanitizerService.class);
    private final PasswordEncoder passwordEncoderMock = mock(PasswordEncoder.class);
    private final UserBusinessService userBusinessService = new UserBusinessService(
            userRepositoryMock,
            userDtoServiceMock,
            groupRepositoryMock,
            mongoTemplateMock,
            inputSanitizerServiceMock,
            passwordEncoderMock);


    private static UserEntity userEntityMock;

    @BeforeEach
    void setUp() {
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
        assertEquals(UserNotFoundException.getErrorMessagePrefix() + " UserId was " + userId, ex.getMessage());
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

        assertEquals(UserNotFoundException.getErrorMessagePrefix() + " UserId was " + id, ex.getMessage());
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
        assertEquals(UserNotFoundException.getErrorMessagePrefix() + " Username was " + validFormat, exception.getMessage());
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
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " Username was not valid.", ex.getMessage());

        // username taken
        userRegisterForm.setUsername(username);
        when(userRepositoryMock.findByLowercaseUsername(username.toLowerCase())).thenReturn(UserEntity.builder().build());
        when(userDtoServiceMock.createDto(any())).thenReturn(User.builder().build());

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " Username already taken.", ex.getMessage());

        //passwords empty
        when(userRepositoryMock.findByLowercaseUsername(username.toLowerCase())).thenReturn(null);
        when(userDtoServiceMock.createDto(any())).thenReturn(null);
        userRegisterForm.setPassword("");
        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " Wanted to change password, but password was not valid.", ex.getMessage());

        userRegisterForm.setPassword("somepassword");
        userRegisterForm.setConfirmationPassword("");
        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " Wanted to change password, but password was not valid.", ex.getMessage());

        //Passwords not valid
        userRegisterForm.setConfirmationPassword(notValidPassword);
        userRegisterForm.setPassword(notValidPassword);

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " Password needs to be a valid SHA-265 hash.", ex.getMessage());

        //Passwords do not match.
        when(inputSanitizerServiceMock.passwordIsValid(password)).thenReturn(true);
        userRegisterForm.setPassword(password);

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " Passwords do not match.", ex.getMessage());

        // group does not exist
        userRegisterForm.setUsername(username);
        userRegisterForm.setConfirmationPassword(userRegisterForm.getPassword());
        when(groupRepositoryMock.getGroupById(420)).thenThrow(IllegalArgumentException.class);

        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " One or more groups do not exist.", ex.getMessage());

        userRegisterForm.setGroupIds(new long[]{Group.SYSTEM.getGroupId()});
        ex = assertThrows(UserNotRegisteredException.class, () ->
                userBusinessService.registerNewUser(userRegisterForm));
        assertEquals(UserNotRegisteredException.getErrorMessagePrefix() + " New users cannot be in group '" + Group.SYSTEM.getDisplayName() + "'.", ex.getMessage());
    }

    @Test
    void registerNewUserWorks() {
        String username = "username";
        String password = "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126";
        String confPassword = "86C9C198F7DF1F0E6633E21A12BCA14730A27070BBCC742FEC8B2B14B44A0126";
        long[] groups = new long[]{0};

        when(inputSanitizerServiceMock.passwordIsValid(password)).thenReturn(true);

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
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " No updates specified.", ex.getMessage());

        UserRegisterForm userRegisterForm1 = UserRegisterForm.builder().build();
        User authenticatedUser1 = User.builder().groups(null).build();
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser1));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Authenticated User is not allowed.", ex.getMessage());

        authenticatedUser.setGroups(new Group[]{Group.UNDEFINED});
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Only Admins are allowed to update other users.", ex.getMessage());

        //user not found with id.
        authenticatedUser.setGroups(new Group[]{Group.ADMIN});
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " User does not exist, use register endpoint.", ex.getMessage());

        when(userRepositoryMock.findByUserId(userId)).thenReturn(UserEntity.builder().groupIds(new long[]{Group.SYSTEM.getGroupId()}).build());
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Runtime users cannot be modified.", ex.getMessage());

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm1, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " No changes were made.", ex.getMessage());
    }

    @Test
    void updateUserNameThrows() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);

        userRegisterForm.setUsername("");
        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Wanted to change username, but username was not valid.", ex.getMessage());

        String validUserName = "ValidUserNameButExists.";
        userRegisterForm.setUsername(validUserName);
        when(userRepositoryMock.findByLowercaseUsername(validUserName.toLowerCase())).thenReturn(dummyEntity);
        when(userDtoServiceMock.createDto(dummyEntity)).thenReturn(User.builder().build());
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Username already taken.", ex.getMessage());
    }

    @Test
    void updateUserNameWorks() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().username("newUserName").build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);

        assertDoesNotThrow(() -> userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));

        // updating the user with the same username works.
        UserRegisterForm anotherOne = UserRegisterForm.builder().username(userEntityMock.getUsername()).build();
        when(userRepositoryMock.findByLowercaseUsername(userEntityMock.getLowercaseUsername())).thenReturn(userEntityMock);

        UserNotUpdatedException exception = assertThrows(UserNotUpdatedException.class,
                () -> userBusinessService.updateUser(userId, anotherOne, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " No changes were made.", exception.getMessage());
    }

    @Test
    void updatePasswordThrows() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(userEntityMock);

        userRegisterForm.setPassword("");
        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Wanted to change password, but password was not valid.", ex.getMessage());

        userRegisterForm.setPassword("somepw");
        userRegisterForm.setConfirmationPassword("");
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Wanted to change password, but password was not valid.", ex.getMessage());

        userRegisterForm.setPassword("somepw");
        userRegisterForm.setConfirmationPassword("somepw");
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Password needs to be a valid SHA-256 hash.", ex.getMessage());

        // equals
        String validPassword = "DefinitlyASHA256Hash";
        userRegisterForm.setPassword(validPassword);

        when(inputSanitizerServiceMock.passwordIsValid(validPassword)).thenReturn(true);
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser), "Passwords do not match.");
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Passwords do not match.", ex.getMessage());
    }

    @Test
    void updatePasswordWorks() {
        String password = "validPassword1234";
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().password(password).confirmationPassword(password).build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("UGABUGA").build();

        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(inputSanitizerServiceMock.passwordIsValid(password)).thenReturn(true);

        assertDoesNotThrow(() -> userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
    }

    @Test
    void updateGroupsThrows() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("password").build();

        long[] groups = new long[]{0};
        userRegisterForm.setGroupIds(groups);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(groupRepositoryMock.getGroupsByIds(groups)).thenReturn(new Group[]{Group.ADMIN});
        UserNotUpdatedException ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Only admins can add users to group Admin.", ex.getMessage());

        groups = new long[]{123032, 1230213};
        userRegisterForm.setGroupIds(groups);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(groupRepositoryMock.getGroupsByIds(groups)).thenThrow(new IllegalArgumentException("id doesnt belong to a group"));
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " One or more groups do not exist.", ex.getMessage());

        long[] systemUser = new long[]{Group.SYSTEM.getGroupId()};
        userRegisterForm.setGroupIds(systemUser);
        when(groupRepositoryMock.getGroupsByIds(systemUser)).thenReturn(new Group[]{Group.SYSTEM});
        ex = assertThrows(UserNotUpdatedException.class, () ->
                userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
        assertEquals(UserNotUpdatedException.getErrorMessagePrefix() + " Users cannot be added to the '" + Group.SYSTEM.getDisplayName() + "' Group", ex.getMessage());
    }

    @Test
    void updateGroupsWorks() {
        final UserRegisterForm userRegisterForm = UserRegisterForm.builder().build();
        long userId = 420;
        User authenticatedUser = User.builder().userId(userId).groups(new Group[]{Group.FAMILY}).build();
        UserEntity dummyEntity = UserEntity.builder().userId(userId).lowercaseUsername("password").build();

        long[] groups = new long[]{0};
        userRegisterForm.setGroupIds(groups);
        when(userRepositoryMock.findByUserId(userId)).thenReturn(dummyEntity);
        when(groupRepositoryMock.getGroupsByIds(groups)).thenReturn(new Group[]{Group.FAMILY});
        assertDoesNotThrow(() -> userBusinessService.updateUser(userId, userRegisterForm, authenticatedUser));
    }

    @Test
    void generateRandomUserIdWorks() {
        long actualValue = userBusinessService.generateRandomUserId();
        assertTrue(0 <= actualValue && actualValue <= UserBusinessService.USER_ID_MAX);
    }
}