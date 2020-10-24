package de.filefighter.rest.domain.user.data.persistance;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "user")
@Data
public class UserEntitiy {

    @MongoId
    private String id;
    private long userId;
    private String username;
    private String password;
    private String refreshToken; //TODO: add valid_until for refreshToken
    private long[] roleIds;

    public UserEntitiy(long userId, String username, String password, String refreshToken, long... roleIds) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.refreshToken = refreshToken;
        this.roleIds = roleIds;
    }
}
