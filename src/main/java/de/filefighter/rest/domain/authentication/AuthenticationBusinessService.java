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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Log4j2
@Service
public class AuthenticationBusinessService {

    private final UserRepository userRepository;
    private final UserDTOService userDtoService;
    private final InputSanitizerService inputSanitizerService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationBusinessService(UserRepository userRepository, UserDTOService userDtoService, InputSanitizerService inputSanitizerService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDtoService = userDtoService;
        this.inputSanitizerService = inputSanitizerService;
        this.passwordEncoder = passwordEncoder;
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
            throw new RequestDidntMeetFormalRequirementsException("Credentials didn't meet formal requirements.");

        String lowerCaseUsername = inputSanitizerService.sanitizeString(split[0].toLowerCase());
        String password = inputSanitizerService.sanitizeString(split[1]);

        if (!inputSanitizerService.passwordIsValid(password))
            throw new UserNotAuthenticatedException("The password didn't match requirements, please hash the password with SHA-256.");

        UserEntity userEntity = userRepository.findByLowercaseUsername(lowerCaseUsername);
        if (null == userEntity)
            throw new UserNotAuthenticatedException("No User found with this username and password.");

        if (!passwordEncoder.matches(password, userEntity.getPassword()))
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
