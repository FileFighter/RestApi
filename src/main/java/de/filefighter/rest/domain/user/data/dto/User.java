package de.filefighter.rest.domain.user.data.dto;
import de.filefighter.rest.domain.user.role.Role;
import lombok.Builder;
import lombok.Data;


@Builder(builderClassName = "UserBuilder", buildMethodName = "create")
@Data
public class User {
    private long id;
    private String username;
    private Role[] roles;

    public User(long id, String username, Role... roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}