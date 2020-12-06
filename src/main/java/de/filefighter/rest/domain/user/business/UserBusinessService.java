package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.exceptions.UserNotUpdatedException;
import de.filefighter.rest.domain.user.group.GroupRepository;
import de.filefighter.rest.domain.user.group.Groups;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.regex.Pattern;

import static de.filefighter.rest.domain.common.InputSanitizerService.stringIsValid;

@Log4j2
@Service
public class UserBusinessService {

    private final UserRepository userRepository;
    private final UserDtoService userDtoService;
    private final GroupRepository groupRepository;
    private final MongoTemplate mongoTemplate;

    public static final int USER_ID_MAX = 99999999;


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
        UserEntity userEntity = userRepository.findByUserIdAndUsername(user.getUserId(), user.getUsername());
        if (null == userEntity)
            throw new UserNotFoundException(user.getUserId());

        String refreshTokenValue = userEntity.getRefreshToken();

        if (!stringIsValid(refreshTokenValue))
            throw new IllegalStateException("RefreshToken was invalid or empty in db.");

        return RefreshToken
                .builder()
                .tokenValue(refreshTokenValue)
                .user(user)
                .build();
    }

    public User findUserByUsername(String username) {
        String lowercaseUsername = username.toLowerCase();

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
            log.info("Username '{}' is free to use.", username);
        }

        if (null != user)
            throw new UserNotRegisteredException("Username already taken.");

        // check pws.
        String password = newUser.getPassword();
        String confirmationPassword = newUser.getConfirmationPassword();

        if (!stringIsValid(password) || !stringIsValid(confirmationPassword))
            throw new UserNotRegisteredException("Wanted to change password, but password was not valid.");

        if (!passwordIsValid(password))
            throw new UserNotRegisteredException("Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.");

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
                .userId(generateRandomUserId())
                .build());
    }

    public boolean passwordIsValid(String password) {
        if (this.passwordCheckDisabled)
            return true;

        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+).{8,20}$");
        return pattern.matcher(password).matches();
    }

    public void updateUser(long userId, UserRegisterForm userToUpdate, User authenticatedUser) {
        if (null == userToUpdate)
            throw new UserNotUpdatedException("No updates specified.");

        if (null == authenticatedUser.getGroups())
            throw new UserNotUpdatedException("Authenticated User is not allowed.");

        boolean authenticatedUserIsAdmin = Arrays.stream(authenticatedUser.getGroups()).anyMatch(g -> g == Groups.ADMIN);
        if (userId != authenticatedUser.getUserId() && !authenticatedUserIsAdmin)
            throw new UserNotUpdatedException("Only Admins are allowed to update other users.");

        UserEntity userEntityToUpdate = userRepository.findByUserId(userId);
        if (null == userEntityToUpdate)
            throw new UserNotUpdatedException("User does not exist, use register endpoint.");

        Update newUpdate = new Update();

        boolean changesWereMade = updateUserName(newUpdate, userToUpdate.getUsername());

        boolean usernameIsValid = stringIsValid(userToUpdate.getUsername());
        String lowerCaseUsername = usernameIsValid ? userToUpdate.getUsername().toLowerCase() : userEntityToUpdate.getLowercaseUsername();
        boolean passwordWasUpdated = updatePassword(newUpdate, userToUpdate.getPassword(), userToUpdate.getConfirmationPassword(), lowerCaseUsername);
        changesWereMade = passwordWasUpdated || changesWereMade;

        boolean userGroupsWereUpdated = updateGroups(newUpdate, userToUpdate.getGroupIds(), authenticatedUserIsAdmin);
        changesWereMade = userGroupsWereUpdated || changesWereMade;

        if (!changesWereMade)
            throw new UserNotUpdatedException("No changes were made.");

        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        mongoTemplate.findAndModify(query, newUpdate, UserEntity.class);
    }

    private boolean updateGroups(Update newUpdate, long[] groupIds, boolean authenticatedUserIsAdmin) {
        if (null != groupIds) {
            try {
                for (Groups group : groupRepository.getGroupsByIds(groupIds)) {
                    if (group == Groups.ADMIN && !authenticatedUserIsAdmin)
                        throw new UserNotUpdatedException("Only admins can add users to group " + Groups.ADMIN.getDisplayName() + ".");
                }
            } catch (IllegalArgumentException exception) {
                throw new UserNotUpdatedException("One or more groups do not exist.");
            }

            newUpdate.set("groupIds", groupIds);
            return true;
        }
        return false;
    }

    private boolean updatePassword(Update newUpdate, String password, String confirmationPassword, String lowercaseUserName) {
        if (null != password) {

            if (!stringIsValid(password) || !stringIsValid(confirmationPassword))
                throw new UserNotUpdatedException("Wanted to change password, but password was not valid.");

            if (!passwordIsValid(password))
                throw new UserNotUpdatedException("Password needs to be at least 8 characters long and, contains at least one uppercase and lowercase letter and a number.");

            if (!password.contentEquals(confirmationPassword))
                throw new UserNotUpdatedException("Passwords do not match.");

            if (password.toLowerCase().contains(lowercaseUserName))
                throw new UserNotUpdatedException("Username must not appear in password.");

            newUpdate.set("password", password);
            //update refreshToken
            String newRefreshToken = AccessTokenBusinessService.generateRandomTokenValue();
            newUpdate.set("refreshToken", newRefreshToken);

            return true;
        }
        return false;
    }

    private boolean updateUserName(Update update, String username) {
        if (null != username) {
            if (!stringIsValid(username))
                throw new UserNotUpdatedException("Wanted to change username, but username was not valid.");

            User user = null;
            try {
                user = this.findUserByUsername(username);
            } catch (UserNotFoundException ignored) {
                log.info("Username '{}' is free to use.", username);
            }

            if (null != user)
                throw new UserNotUpdatedException("Username already taken.");

            update.set("username", username);
            return true;
        }
        return false;
    }

    public long generateRandomUserId() {
        long possibleUserId = 0L;
        boolean userIdIsFree = false;

        while (!userIdIsFree) {
            possibleUserId = new SecureRandom().nextInt(UserBusinessService.USER_ID_MAX);
            UserEntity userEntity = userRepository.findByUserId(possibleUserId);
            if (null == userEntity && possibleUserId > 0)
                userIdIsFree = true;
        }

        return possibleUserId;
    }
}

