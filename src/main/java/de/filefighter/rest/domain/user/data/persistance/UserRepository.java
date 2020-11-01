package de.filefighter.rest.domain.user.data.persistance;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Service;

@Service
public interface UserRepository extends MongoRepository<UserEntity, String> {
    UserEntity findByUserId(long userId);
    UserEntity findByUsernameAndPassword(String username, String password);
    UserEntity findByRefreshToken(String refreshToken);
}
