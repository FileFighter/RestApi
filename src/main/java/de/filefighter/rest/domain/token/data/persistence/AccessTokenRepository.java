package de.filefighter.rest.domain.token.data.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface AccessTokenRepository extends MongoRepository<AccessTokenEntity, String> {
    AccessTokenEntity findByUserId(long userId);
    AccessTokenEntity findByValue(String value);
    AccessTokenEntity findByUserIdAndValue(long userId, String value);
    long deleteByUserId(long userId);
}
