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

public class UserRegistrationSteps extends RestApplicationIntegrationTest {

    private final ObjectMapper objectMapper;

    @Autowired
    public UserRegistrationSteps(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @When("user requests registration with username {string}, password {string} and password confirmation {string} with accessToken {string}")
    public void userRequestsRegistrationWithUsernamePasswordAndPasswordConfirmationWithAccessToken(String username, String password, String passwordConfirmation, String accessToken) throws JsonProcessingException {
        String authHeaderString = AUTHORIZATION_BEARER_PREFIX + accessToken;
        String url = BASE_API_URI + USER_BASE_URI + "register";

        HashMap<String, String> authHeader = new HashMap<>();
        authHeader.put("Authorization", authHeaderString);

        UserRegisterForm updateForm = UserRegisterForm.builder()
                .username(username)
                .confirmationPassword(passwordConfirmation)
                .groupIds(null)
                .password(password)
                .build();


        String postBody = objectMapper.writeValueAsString(updateForm);
        executeRestApiCall(HttpMethod.POST, url, authHeader, postBody);
    }
}
