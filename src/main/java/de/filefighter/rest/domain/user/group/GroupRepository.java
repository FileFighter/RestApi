package de.filefighter.rest.domain.user.group;

import org.springframework.stereotype.Service;

@Service
public class GroupRepository {
    private final Groups[] groups = Groups.values();

    public Groups getGroupById(long id) {
        for (Groups group : groups) {
            if (group.getGroupId() == id) {
                return group;
            }
        }
        throw new IllegalArgumentException("id doesnt belong to a group");
    }

    public Groups[] getGroupsByIds(long... ids){
        Groups[] groups;
        if(ids.length == 0){
            return new Groups[0];
        }
        groups = new Groups[ids.length];

        for (int i = 0; i < ids.length; i++) {
            groups[i] = this.getGroupById(ids[i]);
        }
        return groups;
    }
}
