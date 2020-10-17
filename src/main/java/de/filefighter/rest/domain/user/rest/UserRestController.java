package de.filefighter.rest.domain.user.rest;

import de.filefighter.rest.domain.token.data.dto.AccessToken;
import de.filefighter.rest.domain.token.data.dto.RefreshToken;
import de.filefighter.rest.domain.user.data.dto.User;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import static de.filefighter.rest.config.RestConfiguration.*;

@RestController
@Api(value = "User Rest Controller", tags = { "User" })
@RequestMapping(BASE_API_URI)
public class UserRestController {

    private final static Logger LOG = LoggerFactory.getLogger(UserRestController.class);
    public final static String USER_BASE_URI = "/users/";
    
    private final UserRestServiceInterface userRestService;

    @Autowired
    public UserRestController(UserRestServiceInterface userRestService ) {
        this.userRestService = userRestService;
    }

    @PostMapping(USER_BASE_URI+"register")
    public EntityModel<User> registerNewUser(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX+"admin-token") String accessToken,
            @RequestBody UserRegisterForm newUser){

        LOG.info("Registered new User {}.", newUser);
        return userRestService.registerNewUserWithAccessToken(newUser, accessToken);
    }

    @GetMapping(USER_BASE_URI+"login")
    public EntityModel<RefreshToken> loginUserWithUsernameAndPassword(
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BASIC_PREFIX+"S2V2aW46MTIzNA==") String base64encodedUserAndPassword){

        LOG.info("Requested Login.");
        return userRestService.getRefreshTokenWithUsernameAndPassword(base64encodedUserAndPassword);
    }

    @GetMapping(USER_BASE_URI+"{userId}/login")
    public EntityModel<AccessToken> getAccessTokenAndUserInfoByRefreshTokenAndUserId(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX+"token") String refreshToken){

        LOG.info("Requested refreshing for user {} with token {}.",userId, refreshToken);
        return userRestService.getAccessTokenByRefreshTokenAndUserId(refreshToken, userId);
    }


    @GetMapping(USER_BASE_URI+"{userId}/info")
    public EntityModel<User> getUserInfoWithAccessToken(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX+"token") String accessToken) {

        LOG.info("Requested User {} with token {}.", userId, accessToken);
        return userRestService.getUserByAccessTokenAndUserId(accessToken, userId);
    }

    @PutMapping(USER_BASE_URI+"{userId}/edit")
    public EntityModel<User> updateUserWithAccessToken(
            @PathVariable long userId,
            @RequestHeader(value = "Authorization", defaultValue = AUTHORIZATION_BEARER_PREFIX+"token") String accessToken,
            @RequestBody UserRegisterForm updatedUser) {

        LOG.info("Updated User with the id {} and Token {}, with form {}.", userId, accessToken, updatedUser);
        return userRestService.updateUserByAccessTokenAndUserId(updatedUser, accessToken, userId);
    }
}
