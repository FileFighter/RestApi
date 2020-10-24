package de.filefighter.rest.domain.user.data.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface UserRepository extends MongoRepository<UserEntitiy, String> {
    UserEntitiy findByUserId(long userId);
    UserEntitiy findByUsernameAndPassword(String username, String password);
    UserEntitiy findByRefreshToken(String refreshToken);
}
