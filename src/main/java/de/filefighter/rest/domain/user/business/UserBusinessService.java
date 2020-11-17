package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserAlreadyExistsException;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.group.GroupRepository;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.springframework.stereotype.Service;

import static de.filefighter.rest.domain.common.Utils.stringIsValid;

@Service
public class UserBusinessService {

    private final UserRepository userRepository;
    private final UserDtoService userDtoService;
    private final GroupRepository groupRepository;

    public UserBusinessService(UserRepository userRepository, UserDtoService userDtoService, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
        this.groupRepository = groupRepository;
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public User getUserById(long userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (null == userEntity) {
            throw new UserNotFoundException(userId);
        }

        return userDtoService.createDto(userEntity);
    }

    public RefreshToken getRefreshTokenForUser(User user) {
        UserEntity userEntity = userRepository.findByUserIdAndUsername(user.getId(), user.getUsername());
        if (null == userEntity)
            throw new UserNotFoundException(user.getId());

        String refreshTokenValue = userEntity.getRefreshToken();

        if (!stringIsValid(refreshTokenValue))
            throw new IllegalStateException("RefreshToken was empty in db.");

        return RefreshToken
                .builder()
                .refreshToken(refreshTokenValue)
                .user(user)
                .build();
    }

    public User findUserByUsername(String username) {
        if (!stringIsValid(username))
            throw new RequestDidntMeetFormalRequirementsException("Username was not valid.");

        String lowercaseUsername = username.toLowerCase().replace(" ", "");

        UserEntity entity = userRepository.findByLowercaseUsername(lowercaseUsername);
        if (null == entity)
            throw new UserNotFoundException("User with username '" + username + "' not found.");

        return userDtoService.createDto(entity);
    }

    public void registerNewUser(UserRegisterForm newUser) {
        // check username
        String username = newUser.getUsername();

        User user = null;
        try {
            user = this.findUserByUsername(newUser.getUsername());
        } catch (UserNotFoundException ignored) {
        }

        if (null != user)
            throw new UserAlreadyExistsException("Username already taken.");

        // check pws.
        String password = newUser.getPassword();
        passwordIsValid(password);

        String confirmationPassword = newUser.getConfirmationPassword();
        passwordIsValid(confirmationPassword);

        if (!password.contentEquals(confirmationPassword))
            throw new UserNotRegisteredException("Passwords do not match.");

        //check groups
        long[] userGroups = newUser.getGroupIds();
        if (null == userGroups)
            userGroups = new long[0];

        for (long id : userGroups) {
            try {
                groupRepository.getGroupById(id);
            } catch (IllegalArgumentException exception) {
                throw new UserNotRegisteredException("One or more groups do not exist.");
            }
        }

        //create new user.
        userRepository.save(UserEntity.builder()
                .lowercaseUsername(username.toLowerCase())
                .username(username)
                .password(password)
                .refreshToken(AccessTokenBusinessService.generateRandomTokenValue())
                .userId(getUserCount() + 1)
                .build());
    }

    public void passwordIsValid(String password) {
        //throw new UserNotRegisteredException("Password did not met formal requirements.");
    }
}
