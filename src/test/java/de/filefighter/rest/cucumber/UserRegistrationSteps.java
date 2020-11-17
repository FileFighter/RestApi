package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.TestUtils;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

public class UserRegistrationSteps extends RestApplicationIntegrationTest {
    @When("user requests registration with username {string}, password {string} and password confirmation {string} with accessToken {string}")
    public void userRequestsRegistrationWithUsernamePasswordAndPasswordConfirmationWithAccessToken(String username, String password, String passwordConfirmation, String accessToken) {

        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + "register";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        String postBody= TestUtils.serializeUserRequest(password,null,password,username);
        executeRestApiCall(HttpMethod.POST, url, authHeader,postBody);
    }
}
