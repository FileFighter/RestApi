package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.user.data.dto.UserRegisterForm;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

import static de.filefighter.rest.configuration.RestConfiguration.*;

public class UserEditInformationSteps extends RestApplicationIntegrationTest {

    private final ObjectMapper objectMapper;

    @Autowired
    public UserEditInformationSteps(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @When("user requests change of username with value {string} userId {long} and accessToken {string}")
    public void userRequestsChangeOfUsernameWithValueAndAccessTokenAndId(String newUsername, long userId, String accessToken) throws JsonProcessingException {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        UserRegisterForm updateForm = UserRegisterForm.builder()
                .username(newUsername)
                .confirmationPassword(null)
                .groupIds(null)
                .password(null)
                .build();


        String postBody = objectMapper.writeValueAsString(updateForm);
        executeRestApiCall(HttpMethod.PUT, url, authHeader, postBody);
    }

    @When("user requests change of password with value {string} userId {long} and accessToken {string}")
    public void userRequestsChangeOfPasswordWithValueAndAccessTokenAndId(String newPassword, long userId, String accessToken) throws JsonProcessingException {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);
        UserRegisterForm updateForm = UserRegisterForm.builder()
                .username(null)
                .confirmationPassword(newPassword)
                .groupIds(null)
                .password(newPassword)
                .build();


        String postBody = objectMapper.writeValueAsString(updateForm);
        executeRestApiCall(HttpMethod.PUT, url, authHeader, postBody);
    }

    @When("user requests change of password with no changes, userId {long} and accessToken {string}")
    public void userRequestsChangeOfPasswordWithNoChangesUserIdLongAndAccessTokenString(long userId, String accessToken) throws JsonProcessingException {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + userId + "/edit";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);
        UserRegisterForm updateForm = UserRegisterForm.builder()
                .username(null)
                .confirmationPassword(null)
                .groupIds(null)
                .password(null)
                .build();


        String postBody = objectMapper.writeValueAsString(updateForm);
        executeRestApiCall(HttpMethod.PUT, url, authHeader, postBody);
    }
}
