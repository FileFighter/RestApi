package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.common.Utils;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.exceptions.UserNotUpdatedException;
import de.filefighter.rest.domain.user.group.GroupRepository;
import de.filefighter.rest.domain.user.group.Groups;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Pattern;

import static de.filefighter.rest.domain.common.Utils.stringIsValid;

@Service
public class UserBusinessService {

    private final UserRepository userRepository;
    private final UserDtoService userDtoService;
    private final GroupRepository groupRepository;
    private final MongoTemplate mongoTemplate;


    private static final Logger LOG = LoggerFactory.getLogger(UserBusinessService.class);

    @Value("${filefighter.disable-password-check}")
    public boolean passwordCheckDisabled;

    public UserBusinessService(UserRepository userRepository, UserDtoService userDtoService, GroupRepository groupRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
        this.groupRepository = groupRepository;
        this.mongoTemplate = mongoTemplate;
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
                .tokenValue(refreshTokenValue)
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
            LOG.info("Username '{}' is free to use.", username);
        }

        if (null != user)
            throw new UserNotRegisteredException("Username already taken.");

        // check pws.
        String password = newUser.getPassword();
        if (!passwordIsValid(password))
            throw new UserNotRegisteredException("Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.");

        String confirmationPassword = newUser.getConfirmationPassword();

        if (!password.contentEquals(confirmationPassword))
            throw new UserNotRegisteredException("Passwords do not match.");

        if (password.toLowerCase().contains(username.toLowerCase()))
            throw new UserNotRegisteredException("Username must not appear in password.");

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

    public boolean passwordIsValid(String password) {
        if (!Utils.stringIsValid(password))
            return false;

        if (this.passwordCheckDisabled) return true;

        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+).{8,20}$");
        return pattern.matcher(password).matches();
    }

    public void updateUser(long userId, UserRegisterForm userToUpdate, User authenticatedUser) {
        if (null == userToUpdate)
            throw new UserNotUpdatedException("No updates specified.");

        if (null == authenticatedUser.getGroups())
            throw new UserNotUpdatedException("Authenticated User is not allowed");

        boolean authenticatedUserIsAdmin = Arrays.stream(authenticatedUser.getGroups()).anyMatch(g -> g == Groups.ADMIN);
        if (userId != authenticatedUser.getId() && !authenticatedUserIsAdmin)
            throw new UserNotUpdatedException("Only Admins are allowed to update other users.");

        UserEntity userEntityToUpdate = userRepository.findByUserId(userId);
        Update newUpdate = new Update();
        boolean changesWereMade = false;

        // username
        String username = userToUpdate.getUsername();
        if (null != username) {
            if (!stringIsValid(username))
                throw new UserNotUpdatedException("Wanted to change username, but username was not valid.");

            User user = null;
            try {
                user = this.findUserByUsername(username);
            } catch (UserNotFoundException ignored) {
                LOG.info("Username '{}' is free to use.", username);
            }

            if (null != user)
                throw new UserNotUpdatedException("Username already taken.");

            changesWereMade = true;
            newUpdate.set("username", username);
        }

        // pw
        if (null != userToUpdate.getPassword()) {
            String password = userToUpdate.getPassword();
            String confirmation = userToUpdate.getConfirmationPassword();

            if (!stringIsValid(password) || !stringIsValid(confirmation))
                throw new UserNotUpdatedException("Wanted to change password, but password was not valid.");

            if (!passwordIsValid(password))
                throw new UserNotUpdatedException("Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.");

            if (!password.contentEquals(confirmation))
                throw new UserNotUpdatedException("Passwords do not match.");

            if (password.toLowerCase().contains(userEntityToUpdate.getLowercaseUsername()))
                throw new UserNotUpdatedException("Username must not appear in password.");

            changesWereMade = true;
            newUpdate.set("password", password);

            //update refreshToken
            String newRefreshToken = AccessTokenBusinessService.generateRandomTokenValue();
            newUpdate.set("refreshToken", newRefreshToken);
        }

        // groups
        if (null != userToUpdate.getGroupIds()) {
            try {
                for (Groups group : groupRepository.getGroupsByIds(userToUpdate.getGroupIds())) {
                    if (group == Groups.ADMIN && !authenticatedUserIsAdmin)
                        throw new UserNotUpdatedException("Only admins can add users to group " + Groups.ADMIN.getDisplayName());
                }
            } catch (IllegalArgumentException exception) {
                throw new UserNotUpdatedException("One or more groups do not exist.");
            }

            changesWereMade = true;
            newUpdate.set("groupIds", userToUpdate.getGroupIds());
        }

        if (!changesWereMade)
            throw new UserNotUpdatedException("No changes were made.");

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        mongoTemplate.findAndModify(query, newUpdate, UserEntity.class);
    }
}

