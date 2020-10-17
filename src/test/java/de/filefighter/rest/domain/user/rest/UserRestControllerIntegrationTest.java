package de.filefighter.rest.domain.user.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRestControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void registerNewUser() {
    }

    @Test
    void loginUserWithUsernameAndPassword() {
    }

    @Test
    void getAccessTokenAndUserInfoByRefreshTokenAndUserId() {
    }

    @Test
    void getUserInfoWithAccessToken() {
    }

    @Test
    void updateUserWithAccessToken() {
    }
}