package de.filefighter.rest.domain.user.data.persistance;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "user")
@Getter
@ToString
@Builder
@Setter
public class UserEntity {

    @MongoId
    private final String mongoId;
    private long userId;
    private String username;
    private String lowercaseUsername; // Redundancy for performance tradeoff.
    private String password;
    private String refreshToken;
    private long[] groupIds;

}
