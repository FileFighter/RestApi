package de.filefighter.rest.domain.user.data.persistance;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "user")
@Getter
@ToString
@Builder
public class UserEntity {

    @MongoId
    private final String _id;
    private final long userId;
    private final String username;
    private final String lowercaseUsername;
    private final String password;
    private final String refreshToken; //TODO: add valid_until for refreshToken
    private final long[] groupIds;

}
