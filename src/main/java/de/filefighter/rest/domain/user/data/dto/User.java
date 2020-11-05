package de.filefighter.rest.domain.user.data.dto;
import de.filefighter.rest.domain.user.role.Groups;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class User {
    private long id;
    private String username;
    private Groups[] groups;

    public User(long id, String username, Groups... roles) {
        this.id = id;
        this.username = username;
        this.groups = roles;
    }
}