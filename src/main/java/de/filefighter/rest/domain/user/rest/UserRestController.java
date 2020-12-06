package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import de.filefighter.rest.rest.ServerResponse;
import io.swagger.annotations.Api;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.filefighter.rest.configuration.RestConfiguration.*;

@Log4j2
@RestController
@Api(value = "User Rest Controller", tags = {"User"})
@RequestMapping(BASE_API_URI)
public class UserRestController {

    private final UserRestServiceInterface userRestService;

    @Autowired
    public UserRestController(UserRestServiceInterface userRestService) {
        this.userRestService = userRestService;
    }

    @PostMapping(USER_BASE_URI + "register")
    public ResponseEntity<ServerResponse> registerNewUser(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "admin-token") String accessToken,
            @RequestBody UserRegisterForm newUser) {

        log.info("Registered new User {}.", newUser);
        return userRestService.registerNewUserWithAccessToken(newUser, accessToken);
    }

    @GetMapping(USER_BASE_URI + "login")
    public ResponseEntity<RefreshToken> loginWithUsernameAndPassword(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BASIC_PREFIX + "S2V2aW46MTIzNA==") String base64encodedUserAndPassword) {

        log.info("Requested Login.");
        return userRestService.getRefreshTokenWithUsernameAndPassword(base64encodedUserAndPassword);
    }

    @GetMapping(USER_BASE_URI + "auth")
    public ResponseEntity<AccessToken> getAccessToken(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String refreshToken) {

        log.info("Requested login for token {}.", refreshToken);
        return userRestService.getAccessTokenByRefreshToken(refreshToken);
    }


    @GetMapping(USER_BASE_URI + "{userId}/info")
    public ResponseEntity<User> getUserInfo(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken) {

        log.info("Requested User {} with token {}.", userId, accessToken);
        return userRestService.getUserByUserIdAuthenticateWithAccessToken(accessToken, userId);
    }

    @PutMapping(USER_BASE_URI + "{userId}/edit")
    public ResponseEntity<ServerResponse> updateUser(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken,
            @RequestBody UserRegisterForm updatedUser) {

        log.info("Updated User {} and Token {}, with form {}.", userId, accessToken, updatedUser);
        return userRestService.updateUserByUserIdAuthenticateWithAccessToken(updatedUser, userId, accessToken);
    }

    @GetMapping(USER_BASE_URI + "find")
    public ResponseEntity<User> findUserByUsername(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken,
            @RequestParam(name = "username", value = "username") String username
    ) {
        log.info("Requested finding User with the username {} and Token {}", username, accessToken);
        return userRestService.findUserByUsernameAndAccessToken(username, accessToken);
    }
}
