package de.filefighter.rest.domain.user.data.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "user")
@Data
@Builder
public class UserEntity {

    @MongoId
    private final String mongoId;
    private long userId;
    private String username;
    private String lowercaseUsername; // Redundancy for performance tradeoff.
    private String password;
    private String refreshToken;
    @Builder.Default
    private long[] groupIds = new long[0];

}
