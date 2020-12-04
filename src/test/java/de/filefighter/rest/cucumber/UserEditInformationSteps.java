package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.TestUtils.serializeUserRequest;
import static de.filefighter.rest.configuration.RestConfiguration.*;

public class UserEditInformationSteps extends RestApplicationIntegrationTest {

    @When("user requests change of username with value {string} userId {long} and accessToken {string}")
    public void userRequestsChangeOfUsernameWithValueAndAccessTokenAndId(String newUsername, long userId, String accessToken) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        String postBody = serializeUserRequest(null, null, null, newUsername);
        executeRestApiCall(HttpMethod.PUT, url, authHeader, postBody);
    }

    @When("user requests change of password with value {string} userId {long} and accessToken {string}")
    public void userRequestsChangeOfPasswordWithValueAndAccessTokenAndId(String newPassword, long userId, String accessToken) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);
        String postBody = serializeUserRequest(newPassword, null, newPassword, null);

        executeRestApiCall(HttpMethod.PUT, url, authHeader, postBody);
    }

    @When("user requests change of password with no changes, userId {long} and accessToken {string}")
    public void userRequestsChangeOfPasswordWithNoChangesUserIdLongAndAccessTokenString(long userId, String accessToken) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);
        String postBody = serializeUserRequest(null, null, null, null);

        executeRestApiCall(HttpMethod.PUT, url, authHeader, postBody);
    }
}
