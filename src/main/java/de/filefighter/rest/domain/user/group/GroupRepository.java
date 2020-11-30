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
        throw new IllegalArgumentException("id "+id+" doesnt belong to a group.");
    }

    public Groups[] getGroupsByIds(long... ids){
        Groups[] groupArray;
        if(null == ids || ids.length == 0){
            return new Groups[0];
        }
        groupArray = new Groups[ids.length];

        for (int i = 0; i < ids.length; i++) {
            groupArray[i] = this.getGroupById(ids[i]);
        }
        return groupArray;
    }
}
