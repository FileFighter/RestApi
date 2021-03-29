package de.filefighter.rest.domain.authentication;

import de.filefighter.rest.domain.common.InputSanitizerService;
import de.filefighter.rest.domain.common.exceptions.RequestDidntMeetFormalRequirementsException;
import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.user.business.UserDTOService;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import de.filefighter.rest.domain.user.exceptions.UserNotAuthenticatedException;
import de.filefighter.rest.domain.user.group.Group;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Log4j2
@Service
public class AuthenticationBusinessService {

    private final UserRepository userRepository;
    private final UserDTOService userDtoService;

    public AuthenticationBusinessService(UserRepository userRepository, UserDTOService userDtoService) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
    }

    public User authenticateUserWithUsernameAndPassword(String base64encodedUserAndPassword) {
        String decodedUsernameAndPassword;
        try {
            byte[] decodedValue = Base64.getDecoder().decode(base64encodedUserAndPassword);
            decodedUsernameAndPassword = new String(decodedValue, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            log.warn("Found {} in {}", ex.getMessage(), base64encodedUserAndPassword);
            throw new RequestDidntMeetFormalRequirementsException("Found unsupported character in header.");
        }

        String[] split = decodedUsernameAndPassword.split(":");

        if (split.length != 2)
            throw new RequestDidntMeetFormalRequirementsException("Credentials didnt meet formal requirements.");

        String lowerCaseUsername = InputSanitizerService.sanitizeString(split[0].toLowerCase());
        String password = InputSanitizerService.sanitizeString(split[1]);

        UserEntity userEntity = userRepository.findByLowercaseUsernameAndPassword(lowerCaseUsername, password);
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

    public User authenticateUserWithAccessToken(AccessToken accessToken) {
        UserEntity userEntity = userRepository.findByUserId(accessToken.getUserId());
        if (null == userEntity)
            throw new UserNotAuthenticatedException(accessToken.getUserId());

        return userDtoService.createDto(userEntity);
    }

    public void authenticateUserWithAccessTokenAndGroup(AccessToken accessToken, Group groups) {
        UserEntity userEntity = userRepository.findByUserId(accessToken.getUserId());
        if (null == userEntity)
            throw new UserNotAuthenticatedException(accessToken.getUserId());

        boolean authenticated = false;

        if (null != userEntity.getGroupIds()) {
            for (long group : userEntity.getGroupIds()) {
                if (group == groups.getGroupId()) {
                    authenticated = true;
                    break;
                }
            }
        }

        if (!authenticated)
            throw new UserNotAuthenticatedException("Not in necessary group.");
    }
}
