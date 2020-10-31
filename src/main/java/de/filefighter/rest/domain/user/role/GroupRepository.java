package de.filefighter.rest.domain.user.role;

import org.springframework.stereotype.Service;

@Service
public class GroupRepository {
    private final Groups[] roles = Groups.values();

    //TODO: test this.
    public Groups getRoleById(long id) {
        for (Groups role : roles) {
            if (role.getRoleId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("id doesnt belong to a role");
    }

    public Groups[] getRolesByIds(long... ids){
        Groups[] roles = new Groups[ids.length]; //TODO: check this again.

        for (int i = 0; i < ids.length; i++) {
            roles[i] = this.getRoleById(ids[i]);
        }
        return roles;
    }
}
