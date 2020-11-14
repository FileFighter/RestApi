package de.filefighter.rest.domain.user.business;

import de.filefighter.rest.domain.common.Utils;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.rest.exceptions.RequestDidntMeetFormalRequirementsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BASIC_PREFIX;
import static de.filefighter.rest.configuration.RestConfiguration.AUTHORIZATION_BEARER_PREFIX;

@Service
public class UserAuthorizationService {

    private final UserRepository userRepository;
    private final UserDtoService userDtoService;

    private static final Logger LOG = LoggerFactory.getLogger(UserAuthorizationService.class);

    public UserAuthorizationService(UserRepository userRepository, UserDtoService userDtoService) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
    }

    public User authenticateUserWithUsernameAndPassword(String base64encodedUserAndPassword) {
        String decodedUsernameUndPassword;
        try {
            byte[] decodedValue = Base64.getDecoder().decode(base64encodedUserAndPassword);
            decodedUsernameUndPassword = new String(decodedValue, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException | IllegalArgumentException ex) {
            LOG.warn("Found UnsupportedEncodingException in {}", base64encodedUserAndPassword);
            throw new RuntimeException(ex);
        }

        String[] split = decodedUsernameUndPassword.strip().split(":");

        if (split.length != 2)
            throw new RequestDidntMeetFormalRequirementsException("Credentials didnt meet formal requirements.");

        String username = split[0];
        String password = split[1];

        UserEntity userEntity = userRepository.findByUsernameAndPassword(username, password);
        if (null == userEntity)
            throw new UserNotAuthenticatedException("No User found with this username and password.");

        return userDtoService.createDto(userEntity);
    }

    public User authenticateUserWithRefreshToken(String refreshToken) {
        UserEntity userEntity = userRepository.findByRefreshToken(refreshToken);
        if (null == userEntity)
            throw new UserNotAuthenticatedException("No user found for this Refresh Token.");

        return userDtoService.createDto(userEntity);
    }

    public void authenticateUserWithAccessToken(AccessToken accessToken) {
        UserEntity userEntity = userRepository.findByUserId(accessToken.getUserId());
        if (null == userEntity)
            throw new UserNotAuthenticatedException(accessToken.getUserId());
    }
}
