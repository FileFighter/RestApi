package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

public class CrudFileSystemSteps extends RestApplicationIntegrationTest {

    @When("user requests fileSystemInfo with fileSystemId {long} and accessTokenValue {string}")
    public void userRequestsFileSystemInfoWithFileSystemIdAndUserId(long fileSystemId, String accessTokenValue) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, BASE_API_URI + FS_BASE_URI + fileSystemId + "/info", authHeader);
    }

    @When("user with token {string} wants to delete the fileSystemItem with the fileSystemId {long}")
    public void userWithTokenWantsToDeleteTheFileSystemItemWithTheFileSystemId(String accessTokenValue, long fileSystemId) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.DELETE, BASE_API_URI + FS_BASE_URI + fileSystemId + "/delete", authHeader);
    }

    @When("user with token {string} wants to get the info of fileSystemItem with the fileSystemId {long}")
    public void userWithTokenWantsToGetTheInfoOfFileSystemItemWithTheFileSystemId(String accessTokenValue, long fileSystemId) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, BASE_API_URI + FS_BASE_URI + fileSystemId + "/info", authHeader);

    }

    @When("user with token {string} searches for {string}")
    public void userWithTokenSearchesFor(String accessTokenValue, String searchValue) throws UnsupportedEncodingException {

        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, BASE_API_URI + FS_BASE_URI  + "/search?name="+ URLEncoder.encode(searchValue, StandardCharsets.UTF_8.toString()), authHeader);
    }
}
