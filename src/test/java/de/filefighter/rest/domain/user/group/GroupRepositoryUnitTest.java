package de.filefighter.rest.domain.user.group;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupRepositoryUnitTest {

    private final GroupRepository groupRepository = new GroupRepository();

    @Test
    void getGroupByIdThrows() {
        long id = 900;
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                groupRepository.getGroupById(id));
        assertEquals("GroupId " + id + " doesnt belong to a group.", ex.getMessage());
    }

    @Test
    void getGroupByIdWorksCorrectly() {
        Group expectedGroup = Group.FAMILY;
        long groupId = expectedGroup.getGroupId();

        Group actualGroup = groupRepository.getGroupById(groupId);

        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void getGroupsByIdsReturnsEmptyArrayOnEmptyArgs() {
        int actualLength = groupRepository.getGroupsByIds().length;
        assertEquals(0, actualLength);
    }

    @Test
    void getGroupsByIdsReturnsArray() {
        Group[] expectedGroups = new Group[]{Group.FAMILY, Group.ADMIN};
        Group[] actualGroups = groupRepository.getGroupsByIds(0, 1);

        // contents are equal
        assertEquals(0, Arrays.compare(expectedGroups, actualGroups));
    }
}