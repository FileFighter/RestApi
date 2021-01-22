package de.filefighter.rest.domain.user.data.dto;

import de.filefighter.rest.domain.user.group.Group;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class User {
    @Builder.Default
    private long userId = -1;
    private String username;
    @Builder.Default
    private Group[] groups = new Group[0];

    public User(long userId, String username, Group... groups) {
        this.userId = userId;
        this.username = username;
        this.groups = groups;
    }
}