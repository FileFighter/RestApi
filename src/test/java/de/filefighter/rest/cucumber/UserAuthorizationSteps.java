package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserAuthorizationSteps extends CucumberIntegrationTest {

    @When("user requests login with username {string} and password {string}")
    public void userRequestsLoginWithUsernameAndPassword(String username, String password) {
    }

    @When("user requests accessToken with refreshToken {string} and userId {long}")
    public void userRequestsAccessTokenWithRefreshTokenAndUserId(String refreshTokenValue, long userId) {
    }

    @And("response contains valid accessToken")
    public void responseContainsValidAccessToken() {
    }

    @When("user requests userInfo with accessToken {string} and userId {long}")
    public void userRequestsUserInfoWithAccessTokenAndUserId(String accessTokenValue, long userId) {
    }
}
