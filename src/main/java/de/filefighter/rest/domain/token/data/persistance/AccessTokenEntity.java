package de.filefighter.rest.domain.token.data.persistance;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "token")
@Data
@Builder
public class AccessTokenEntity {

    @MongoId
    private String id;
    private String value;
    private long userId;
    private long validUntil;

}