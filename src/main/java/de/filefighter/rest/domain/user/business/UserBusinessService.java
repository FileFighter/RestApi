package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserBusinessService {
    private final UserRepository userRepository;

    public UserBusinessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public long getUserCount(){
        return userRepository.count();
    }
}
