package de.filefighter.rest.domain.token.data.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface AccessTokenRepository extends MongoRepository<AccessTokenEntity, String> {
    AccessTokenEntity findByUserId(long userId);
    AccessTokenEntity findByValue(String value);
    void deleteByUserId(long userId);
}
