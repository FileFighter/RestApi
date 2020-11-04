package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.common.DtoServiceInterface;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.role.GroupRepository;
import org.springframework.stereotype.Service;

@Service
public class UserDtoService implements DtoServiceInterface<User, UserEntity> {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public UserDtoService(GroupRepository groupRepository, UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    @Override
    public User createDto(UserEntity entity) {
        return User
                .builder()
                .id(entity.getUserId())
                .username(entity.getUsername())
                .groups(groupRepository.getRolesByIds(entity.getGroupIds()))
                .build();
    }

    @Override
    public UserEntity findEntity(User dto) {
        UserEntity userEntity = userRepository.findByUserIdAndUsername(dto.getId(), dto.getUsername());
        if (null == userEntity)
            throw new UserNotFoundException(dto.getId());

        return userEntity;
    }
}
