package de.filefighter.rest.domain.token.data.persistence;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document(collection = "token")
@Data
@Builder
public class AccessTokenEntity {

    @MongoId
    private String mongoId;
    private String value;
    @Builder.Default
    private long userId = -1;
    private long validUntil;

}