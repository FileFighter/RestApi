package de.filefighter.rest.domain.user.data.dto;
import de.filefighter.rest.domain.user.group.Groups;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class User {
    private long userId;
    private String username;
    private Groups[] groups;

    public User(long userId, String username, Groups... groups) {
        this.userId = userId;
        this.username = username;
        this.groups = groups;
    }
}