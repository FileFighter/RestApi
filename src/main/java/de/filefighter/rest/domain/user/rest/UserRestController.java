package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.filefighter.rest.configuration.RestConfiguration.*;

@RestController
@Api(value = "User Rest Controller", tags = {"User"})
@RequestMapping(BASE_API_URI)
public class UserRestController {

    private final static Logger LOG = LoggerFactory.getLogger(UserRestController.class);

    private final UserRestServiceInterface userRestService;

    @Autowired
    public UserRestController(UserRestServiceInterface userRestService) {
        this.userRestService = userRestService;
    }

    @PostMapping(USER_BASE_URI + "register")
    public ResponseEntity<User> registerNewUser(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "admin-token") String accessToken,
            @RequestBody UserRegisterForm newUser) {

        LOG.info("Registered new User {}.", newUser);
        return userRestService.registerNewUserWithAccessToken(newUser, accessToken);
    }

    @GetMapping(USER_BASE_URI + "login")
    public ResponseEntity<RefreshToken> loginUserWithUsernameAndPassword(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BASIC_PREFIX + "S2V2aW46MTIzNA==") String base64encodedUserAndPassword) {

        LOG.info("Requested Login.");
        return userRestService.getRefreshTokenWithUsernameAndPassword(base64encodedUserAndPassword);
    }

    @GetMapping(USER_BASE_URI + "{userId}/login")
    public ResponseEntity<AccessToken> getAccessTokenAndUserInfoByRefreshTokenAndUserId(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String refreshToken) {

        LOG.info("Requested login for user {} with token {}.", userId, refreshToken);
        return userRestService.getAccessTokenByRefreshTokenAndUserId(refreshToken, userId);
    }


    @GetMapping(USER_BASE_URI + "{userId}/info")
    public ResponseEntity<User> getUserInfoWithAccessToken(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken) {

        LOG.info("Requested User {} with token {}.", userId, accessToken);
        return userRestService.getUserByAccessTokenAndUserId(accessToken, userId);
    }

    @PutMapping(USER_BASE_URI + "{userId}/edit")
    public ResponseEntity<User> updateUserWithAccessToken(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken,
            @RequestBody UserRegisterForm updatedUser) {

        LOG.info("Updated User with the id {} and Token {}, with form {}.", userId, accessToken, updatedUser);
        return userRestService.updateUserByAccessTokenAndUserId(updatedUser, accessToken, userId);
    }

    @GetMapping(USER_BASE_URI + "find")
    public ResponseEntity<User> findUserByUsername(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX + "token") String accessToken,
            @RequestParam(name = "username", value = "username") String username
    ) {
        LOG.info("Requested finding User with the username {} and Token {}", username,  accessToken);
        return userRestService.findUserByUsernameAndAccessToken(username, accessToken);
    }
}
