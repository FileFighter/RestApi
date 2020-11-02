package de.filefighter.rest.domain.user.data.persistance;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "user")
@Data
@Builder
public class UserEntity {

    @MongoId
    private String id;
    private long userId;
    private String username;
    private String password;
    private String refreshToken; //TODO: add valid_until for refreshToken
    private long[] roleIds;


}
