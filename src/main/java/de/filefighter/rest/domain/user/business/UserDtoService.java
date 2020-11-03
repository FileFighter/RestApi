package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.common.DtoServiceInterface;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import org.springframework.stereotype.Service;

@Service
public class UserDtoService implements DtoServiceInterface<User, UserEntity> {

    @Override
    public User createDto(UserEntity entity) {
        return null;
    }

    @Override
    public UserEntity createEntity(User dto) {
        return null;
    }
}
