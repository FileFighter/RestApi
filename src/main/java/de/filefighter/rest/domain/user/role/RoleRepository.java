package de.filefighter.rest.domain.user.role;

import org.springframework.stereotype.Service;

@Service
public class RoleRepository {
    private final Role[] roles = Role.values();

    //TODO: test this.
    public Role getRoleById(long id) {
        for (Role role : roles) {
            if (role.getRoleId() == id) {
                return role;
            }
        }
        throw new IllegalArgumentException("id doesnt belong to a role");
    }

    public Role[] getRolesByIds(long... ids){
        Role[] roles = new Role[ids.length]; //TODO: check this again.

        for (int i = 0; i < ids.length; i++) {
            roles[i] = this.getRoleById(ids[i]);
        }
        return roles;
    }
}
