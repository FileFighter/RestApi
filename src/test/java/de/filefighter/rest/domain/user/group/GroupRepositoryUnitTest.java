package de.filefighter.rest.domain.user.group;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupRepositoryUnitTest {

    private final GroupRepository groupRepository = new GroupRepository();

    @Test
    void getGroupByIdThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                groupRepository.getGroupById(900));
    }

    @Test
    void getGroupByIdWorksCorrectly() {
        Groups expectedGroup = Groups.FAMILY;
        long groupId = expectedGroup.getGroupId();

        Groups actualGroup = groupRepository.getGroupById(groupId);

        assertEquals(expectedGroup, actualGroup);
    }

    @Test
    void getGroupsByIdsReturnsEmptyArrayOnEmptyArgs() {
        int actualLength = groupRepository.getGroupsByIds().length;
        assertEquals(0, actualLength);
    }

    @Test
    void getGroupsByIdsReturnsArray() {
        Groups[] expectedGroups = new Groups[]{Groups.FAMILY, Groups.ADMIN};
        Groups[] actualGroups = groupRepository.getGroupsByIds(0, 1);

        // contents are equal
        assertEquals(0, Arrays.compare(expectedGroups, actualGroups));
    }
}