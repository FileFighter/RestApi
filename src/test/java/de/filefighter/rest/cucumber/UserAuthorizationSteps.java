package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenEntity;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.bson.internal.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;
import static de.filefighter.rest.configuration.RestConfiguration.*;
import static de.filefighter.rest.domain.token.business.AccessTokenBusinessService.ACCESS_TOKEN_DURATION_IN_SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserAuthorizationSteps extends RestApplicationIntegrationTest {

    private final ObjectMapper objectMapper;
    private final AccessTokenRepository accessTokenRepository;

    @Autowired
    public UserAuthorizationSteps(AccessTokenRepository accessTokenRepository) {
        this.objectMapper = new ObjectMapper();
        this.accessTokenRepository = accessTokenRepository;
    }


    @Given("accessToken with value {string} exists for user {long} and is valid until {long}")
    public void accessTokenWithValueExistsForUserAndIsValidUntil(String tokenValue, long userId, long validUntil) {
        accessTokenRepository.save(AccessTokenEntity.builder()
                .userId(userId)
                .value(tokenValue)
                .validUntil(validUntil)
                .build());
    }

    @Given("accessToken with value {string} exists for user {long}")
    public void accessTokenWithValueExistsForUser(String tokenValue, long userId) {
        accessTokenRepository.save(AccessTokenEntity.builder()
                .userId(userId)
                .value(tokenValue)
                .validUntil(Instant.now().getEpochSecond() + ACCESS_TOKEN_DURATION_IN_SECONDS).build());
    }

    @When("user requests login with username {string} and password {string}")
    public void userRequestsLoginWithUsernameAndPassword(String username, String password) {
        String authString = username + ":" + password;
        String base64encoded = Base64.encode(authString.getBytes());
        base64encoded = AUTHORIZATION_BASIC_PREFIX + base64encoded;

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", base64encoded);

        String url = BASE_API_URI + USER_BASE_URI + "login";

        executeRestApiCall(HttpMethod.GET, url, authHeader);
    }

    @When("user requests accessToken with refreshToken {string}")
    public void userRequestsAccessTokenWithRefreshTokenAndUserId(String refreshTokenValue) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + refreshTokenValue;
        String url = BASE_API_URI + USER_BASE_URI + "auth";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, url, authHeader);
    }

    @When("user requests userInfo with accessToken {string} and userId {long}")
    public void userRequestsUserInfoWithAccessTokenAndUserId(String accessTokenValue, long userId) {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessTokenValue;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/info";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        executeRestApiCall(HttpMethod.GET, url, authHeader);
    }

    @And("response contains valid accessToken for user {long}")
    public void responseContainsValidAccessTokenForUser(long userId) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        String tokenValue = rootNode.get("tokenValue").asText();
        long actualUserId = rootNode.get("userId").asLong();
        long validUntil = rootNode.get("validUntil").asLong();

        int expectedTokenLength = UUID.randomUUID().toString().length();
        int actualTokenLength = tokenValue.length();
        boolean isStillViable = validUntil > Instant.now().getEpochSecond();

        assertEquals(expectedTokenLength, actualTokenLength);
        assertTrue(isStillViable);
        assertEquals(userId, actualUserId);
    }

    @And("response contains refreshToken {string} and the user with userId {long}")
    public void responseContainsRefreshTokenAndTheUserWithId(String refreshToken, long userId) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        String actualRefreshToken = rootNode.get("tokenValue").asText();
        JsonNode userNode = rootNode.get("user");
        long actualUserId = userNode.get("userId").asLong();

        assertEquals(userId, actualUserId);
        assertEquals(refreshToken, actualRefreshToken);
    }

    @And("response contains valid accessToken for user {long} with a different value than {string}")
    public void responseContainsValidAccessTokenForUserWithADifferentValueThan(long userId, String differentTokenValue) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        String actualTokenValue = rootNode.get("tokenValue").asText();
        long actualUserId = rootNode.get("userId").asLong();

        assertEquals(userId, actualUserId);
        assertNotEquals(differentTokenValue, actualTokenValue);
    }
}
