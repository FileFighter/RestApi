package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

public class FindUserSteps extends RestApplicationIntegrationTest {
    @When("user with accessToken {string} searches user with search-value {string}")
    public void userWithAccessTokenSearchesUserWithSearchValue(String accessToken, String search_value) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + "/find?username="+search_value;

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, url, authHeader);
    }
}
