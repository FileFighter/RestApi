package de.filefighter.rest.domain.user.group;

import org.springframework.stereotype.Service;

@Service
public class GroupRepository {
    private final Group[] groups = Group.values();

    public Group getGroupById(long id) {
        for (Group group : groups) {
            if (group.getGroupId() == id) {
                return group;
            }
        }
        throw new IllegalArgumentException("GroupId " + id + " doesnt belong to a group.");
    }

    public Group[] getGroupsByIds(long... ids) {
        Group[] groupArray;
        if (null == ids || ids.length == 0) {
            return new Group[0];
        }
        groupArray = new Group[ids.length];

        for (int i = 0; i < ids.length; i++) {
            groupArray[i] = this.getGroupById(ids[i]);
        }
        return groupArray;
    }
}
