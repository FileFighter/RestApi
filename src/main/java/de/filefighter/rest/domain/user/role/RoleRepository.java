package de.filefighter.rest.domain.user.role;

import org.springframework.stereotype.Service;

import static de.filefighter.rest.domain.user.role.Role.*;

@Service
public class RoleRepository {
    private final Role[] roles = new Role[]{ADMIN, FAMILY};

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
