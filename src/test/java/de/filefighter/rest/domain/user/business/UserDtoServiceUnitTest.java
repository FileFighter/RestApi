package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.group.GroupRepository;
import de.filefighter.rest.domain.user.group.Groups;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDtoServiceUnitTest {

    private final GroupRepository groupRepositoryMock = mock(GroupRepository.class);
    private final UserRepository userRepositoryMock = mock(UserRepository.class);
    private UserDtoService userDtoService;

    @BeforeEach
    void setUp() {
        userDtoService = new UserDtoService(groupRepositoryMock, userRepositoryMock);
    }

    @Test
    void createDto() {
        long userId = 0;
        String username = "kevin";
        long[] groups = new long[]{0};
        UserEntity dummyEntity = UserEntity.builder().userId(userId).groupIds(groups).username(username).build();

        when(groupRepositoryMock.getGroupsByIds(groups)).thenReturn(new Groups[]{Groups.FAMILY});

        User actualUser = userDtoService.createDto(dummyEntity);
        assertEquals(userId, actualUser.getId());
        assertEquals(username, actualUser.getUsername());
        assertEquals(groups[0], actualUser.getGroups()[0].getGroupId());
    }

    @Test
    void findEntityThrowsException() {
        long userId = 0;
        String username = "kevin";
        User user = User.builder().username(username).id(userId).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(null);

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                userDtoService.findEntity(user));
        assertEquals("Could not find user with userId 0.", ex.getMessage());
    }

    @Test
    void findEntityWorksCorrectly() {
        long userId = 0;
        String username = "kevin";
        User user = User.builder().username(username).id(userId).build();
        UserEntity expected = UserEntity.builder().username(username).userId(userId).build();

        when(userRepositoryMock.findByUserIdAndUsername(userId, username)).thenReturn(expected);
        UserEntity actual = userDtoService.findEntity(user);

        assertEquals(expected, actual);
    }
}