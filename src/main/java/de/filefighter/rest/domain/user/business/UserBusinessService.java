package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.token.business.AccessTokenBusinessService;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.domain.user.exceptions.UserNotRegisteredException;
import de.filefighter.rest.domain.user.exceptions.UserNotUpdatedException;
import de.filefighter.rest.domain.user.group.Group;
import de.filefighter.rest.domain.user.group.GroupRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;

import static de.filefighter.rest.domain.common.InputSanitizerService.stringIsValid;

@Service
public class UserBusinessService {

    public static final int USER_ID_MAX = 99999999;
    private final UserRepository userRepository;
    private final UserDTOService userDtoService;
    private final GroupRepository groupRepository;
    private final MongoTemplate mongoTemplate;
    private final InputSanitizerService inputSanitizerService;
    private final PasswordEncoder passwordEncoder;

    public UserBusinessService(UserRepository userRepository, UserDTOService userDtoService, GroupRepository groupRepository, MongoTemplate mongoTemplate, InputSanitizerService inputSanitizerService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
        this.groupRepository = groupRepository;
        this.mongoTemplate = mongoTemplate;
        this.inputSanitizerService = inputSanitizerService;
        this.passwordEncoder = passwordEncoder;
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
        UserEntity entity = getUserWithUsername(username);
        if (null == entity)
            throw new UserNotFoundException(username);

        return userDtoService.createDto(entity);
    }

    public UserEntity registerNewUser(UserRegisterForm newUser) {
        String username = newUser.getUsername();

        if (!stringIsValid(username))
            throw new UserNotRegisteredException("Username was not valid.");

        if (null != this.getUserWithUsername(username))
            throw new UserNotRegisteredException("Username already taken.");

        // check pws.
        String password = newUser.getPassword();
        String confirmationPassword = newUser.getConfirmationPassword();

        if (!stringIsValid(password) || !stringIsValid(confirmationPassword))
            throw new UserNotRegisteredException("Wanted to change password, but password was not valid.");

        if (!inputSanitizerService.passwordIsValid(password))
            throw new UserNotRegisteredException("Password needs to be a valid SHA-265 hash.");

        if (!password.contentEquals(confirmationPassword))
            throw new UserNotRegisteredException("Passwords do not match.");

        //check groups
        long[] userGroups = newUser.getGroupIds();
        if (null == userGroups)
            userGroups = new long[0];

        for (long id : userGroups) {
            try {
                if (id == Group.SYSTEM.getGroupId())
                    throw new UserNotRegisteredException("New users cannot be in group '" + Group.SYSTEM.getDisplayName() + "'.");

                groupRepository.getGroupById(id);
            } catch (IllegalArgumentException exception) {
                throw new UserNotRegisteredException("One or more groups do not exist.");
            }
        }

        // hash password.
        String hashedPassword = passwordEncoder.encode(password);

        //create new user.
        return userRepository.save(UserEntity.builder()
                .lowercaseUsername(username.toLowerCase())
                .username(username)
                .groupIds(userGroups)
                .password(hashedPassword)
                .refreshToken(AccessTokenBusinessService.generateRandomTokenValue())
                .userId(generateRandomUserId())
                .build());
    }

    /**
     * @param username username to find.
     * @return null or the found user.
     */
    public UserEntity getUserWithUsername(String username) {
        String lowercaseUsername = username.toLowerCase();
        return userRepository.findByLowercaseUsername(lowercaseUsername);
    }

    public void updateUser(long userId, UserRegisterForm userToUpdate, User authenticatedUser) {
        if (null == userToUpdate)
            throw new UserNotUpdatedException("No updates specified.");

        if (null == authenticatedUser.getGroups())
            throw new UserNotUpdatedException("Authenticated User is not allowed.");

        boolean authenticatedUserIsAdmin = Arrays.stream(authenticatedUser.getGroups()).anyMatch(g -> g == Group.ADMIN);
        if (userId != authenticatedUser.getUserId() && !authenticatedUserIsAdmin)
            throw new UserNotUpdatedException("Only Admins are allowed to update other users.");

        UserEntity userEntityToUpdate = userRepository.findByUserId(userId);
        if (null == userEntityToUpdate)
            throw new UserNotUpdatedException("User does not exist, use register endpoint.");

        if (Arrays.stream(userEntityToUpdate.getGroupIds()).anyMatch(id -> id == Group.SYSTEM.getGroupId()))
            throw new UserNotUpdatedException("Runtime users cannot be modified.");

        Update newUpdate = new Update();

        boolean changesWereMade = updateUserName(newUpdate, userToUpdate.getUsername());
        boolean passwordWasUpdated = updatePassword(newUpdate, userToUpdate.getPassword(), userToUpdate.getConfirmationPassword());
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
        if (null != groupIds && groupIds.length != 0) {
            try {
                for (Group group : groupRepository.getGroupsByIds(groupIds)) {
                    if (group == Group.SYSTEM)
                        throw new UserNotUpdatedException("Users cannot be added to the '" + Group.SYSTEM.getDisplayName() + "' Group");
                    if (group == Group.ADMIN && !authenticatedUserIsAdmin)
                        throw new UserNotUpdatedException("Only admins can add users to group " + Group.ADMIN.getDisplayName() + ".");
                }
            } catch (IllegalArgumentException exception) {
                throw new UserNotUpdatedException("One or more groups do not exist.");
            }

            newUpdate.set("groupIds", groupIds);
            return true;
        }
        return false;
    }

    private boolean updatePassword(Update newUpdate, String password, String confirmationPassword) {
        if (null != password) {

            if (!stringIsValid(password) || !stringIsValid(confirmationPassword))
                throw new UserNotUpdatedException("Wanted to change password, but password was not valid.");

            if (!inputSanitizerService.passwordIsValid(password))
                throw new UserNotUpdatedException("Password needs to be a valid SHA-256 hash.");

            if (!password.contentEquals(confirmationPassword))
                throw new UserNotUpdatedException("Passwords do not match.");

            // hash password.
            String hashedPassword = passwordEncoder.encode(password);

            newUpdate.set("password", hashedPassword);
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

            if (null != getUserWithUsername(username))
                throw new UserNotUpdatedException("Username already taken.");

            update.set("username", username);
            update.set("lowercaseUsername", username.toLowerCase());
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

