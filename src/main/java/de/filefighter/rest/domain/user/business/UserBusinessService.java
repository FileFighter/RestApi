package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.exceptions.UserNotFoundException;
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

    private static final Logger LOG = LoggerFactory.getLogger(UserBusinessService.class);

    public UserBusinessService(UserRepository userRepository, UserDtoService userDtoService) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
    }

    public long getUserCount() {
        return userRepository.count();
    }

    public User getUserByUsernameAndPassword(String base64encodedUserAndPasswordWithHeaderPrefix) {
        if (!stringIsValid(base64encodedUserAndPasswordWithHeaderPrefix))
            throw new UserNotAuthenticatedException("Header was empty.");

        //TODO: maybe filter unsupported characters?
        if (!base64encodedUserAndPasswordWithHeaderPrefix.matches("^" + AUTHORIZATION_BASIC_PREFIX + "[^\\s](.*)$"))
            throw new UserNotAuthenticatedException("Header does not contain '" + AUTHORIZATION_BASIC_PREFIX + "', or format is invalid.");

        String[] split = base64encodedUserAndPasswordWithHeaderPrefix.split(AUTHORIZATION_BASIC_PREFIX);

        base64encodedUserAndPasswordWithHeaderPrefix = split[1];
        String decodedUsernameUndPassword;
        try {
            byte[] decodedValue = Base64.getDecoder().decode(base64encodedUserAndPasswordWithHeaderPrefix);
            decodedUsernameUndPassword = new String(decodedValue, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException | IllegalArgumentException ex) {
            LOG.warn("Found UnsupportedEncodingException in {}", base64encodedUserAndPasswordWithHeaderPrefix);
            throw new RuntimeException(ex);
        }

        split = decodedUsernameUndPassword.strip().split(":");

        if (split.length != 2)
            throw new UserNotAuthenticatedException("Credentials didnt meet formal requirements.");

        String username = split[0];
        String password = split[1];

        UserEntity userEntity = userRepository.findByUsernameAndPassword(username, password);
        if (null == userEntity)
            throw new UserNotFoundException("No User found with this username and password.");

        return userDtoService.createDto(userEntity);
    }

    public RefreshToken getRefreshTokenForUser(User user) {
        UserEntity userEntity = userRepository.findByUserIdAndUsername(user.getId(), user.getUsername());
        if (null == userEntity)
            throw new UserNotFoundException();

        String refreshTokenValue = userEntity.getRefreshToken();

        if (!stringIsValid(refreshTokenValue))
            throw new IllegalStateException("RefreshToken was empty in db.");

        return RefreshToken
                .builder()
                .refreshToken(refreshTokenValue)
                .user(user)
                .build();
    }

    public User getUserByRefreshTokenAndUserId(String refreshToken, long userId) {
        if (!stringIsValid(refreshToken))
            throw new UserNotAuthenticatedException("RefreshToken was not valid.");

        UserEntity userEntity = userRepository.findByRefreshTokenAndUserId(refreshToken, userId);
        if (null == userEntity)
            throw new UserNotFoundException(userId);

        return userDtoService.createDto(userEntity);
    }

    public User getUserByAccessTokenAndUserId(AccessToken accessToken, long userId) {
        if (accessToken.getUserId() != userId)
            throw new UserNotAuthenticatedException(userId);

        UserEntity userEntity = userRepository.findByUserId(userId);
        if (null == userEntity)
            throw new UserNotFoundException(userId);

        return userDtoService.createDto(userEntity);
    }
}
