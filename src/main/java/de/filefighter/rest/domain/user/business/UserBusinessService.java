package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.sql.Ref;
import java.util.Base64;
import java.util.UUID;

import static de.filefighter.rest.configuration.RestConfiguration.*;
import static de.filefighter.rest.domain.common.Utils.stringIsValid;

@Service
public class UserBusinessService {

    private final UserRepository userRepository;
    private final UserDtoService userDtoService;

    public UserBusinessService(UserRepository userRepository, UserDtoService userDtoService) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
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
}
