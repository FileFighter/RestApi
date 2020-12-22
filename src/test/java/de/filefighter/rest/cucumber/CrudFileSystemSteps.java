package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

public class CrudFileSystemSteps extends RestApplicationIntegrationTest {

    @When("user requests fileSystemInfo with fileSystemId {long} and accessTokenValue {string}")
    public void userRequestsFileSystemInfoWithFileSystemIdAndUserId(long fileSystemId, String accessTokenValue) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;
        String url = BASE_API_URI + USER_BASE_URI + "auth";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, BASE_API_URI + FS_BASE_URI + fileSystemId + "/info", authHeader);
    }
}
