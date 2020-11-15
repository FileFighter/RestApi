package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.RestApplicationIntegrationTest.*;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

public class UserEditInformationSteps extends RestApplicationIntegrationTest {
    @When("user requests change of username with value {string} and accessToken {string} and id {string}")
    public void userRequestsChangeOfUsernameWithValueAndAccessTokenAndId(String newUsername, String accessToken, String userId) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";


        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);




        String postBody="{" +
                "  \"groupIds\": [" +
                "    0" +
                "  ]," +
                "  \"username\": \""+newUsername+"\"" +
                "}";

        executeRestApiCall(HttpMethod.GET, url, authHeader,postBody);

    }

    @When("user requests change of password with value {string} and accessToken {string} and id {string}")
    public void userRequestsChangeOfPasswordWithValueAndAccessTokenAndId(String newPassword, String accessToken, String userId) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);


        String postBody="{\n" +
                "  \"confirmationPassword\": \""+newPassword+"\"," +
                "  \"groupIds\": [" +
                "    0" +
                "  ]," +
                "  \"password\": \""+newPassword+"\"," +
                "}";

        executeRestApiCall(HttpMethod.GET, url, authHeader,postBody);
    }
}
