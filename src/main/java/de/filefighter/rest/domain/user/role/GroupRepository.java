package de.filefighter.rest.domain.user.role;

import org.springframework.stereotype.Service;

@Service
public class GroupRepository {
    private final Groups[] roles = Groups.values();

    //TODO: test this.
    public Groups getRoleById(long id) {
        for (Groups role : roles) {
            if (role.getGroupId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("id doesnt belong to a role");
    }

    public Groups[] getRolesByIds(long... ids){
        Groups[] roles;
        if(null == ids){
            return new Groups[0];
        }
        roles = new Groups[ids.length];

        for (int i = 0; i < ids.length; i++) {
            roles[i] = this.getRoleById(ids[i]);
        }
        return roles;
    }
}
