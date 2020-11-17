package de.filefighter.rest;

import de.filefighter.rest.domain.filesystem.rest.FileSystemRestController;
import de.filefighter.rest.domain.health.rest.SystemHealthRestController;
import de.filefighter.rest.domain.permission.rest.PermissionRestController;
import de.filefighter.rest.domain.user.rest.UserRestController;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("SameParameterValue")
@ActiveProfiles("test")
@SpringBootTest(classes = RestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestApplicationIntegrationTest {

    protected static ResponseResults latestResponse = null;

    @LocalServerPort
    private int port;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    SystemHealthRestController healthController;

    @Autowired
    UserRestController userController;

    @Autowired
    FileSystemRestController fileSystemRestController;

    @Autowired
    PermissionRestController permissionRestController;

    @Test
    void contextLoads() {
        assertThat(healthController).isNotNull();
        assertThat(userController).isNotNull();
        assertThat(fileSystemRestController).isNotNull();
        assertThat(permissionRestController).isNotNull();
    }

    // Inspired by https://github.com/eugenp/tutorials/tree/master/spring-cucumber

    protected void executeRestApiCall(HttpMethod httpMethod, String url) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        executeRequest(httpMethod, url, headers,null);
    }

    protected void executeRestApiCall(HttpMethod httpMethod, String url, Map<String, String> headers) {
        executeRequest(httpMethod, url, headers,null);
    }
    protected void executeRestApiCall(HttpMethod httpMethod, String url, Map<String, String> headers,String postBody) {
        executeRequest(httpMethod, url, headers, postBody);
    }

    private void executeRequest(HttpMethod httpMethod, String url, Map<String, String> headers,String postBody) {
        final HeaderSettingRequestCallback requestCallback = new HeaderSettingRequestCallback(headers);
        if (postBody!=null){
            requestCallback.setBody(postBody);
        }
        final ResponseResultErrorHandler errorHandler = new ResponseResultErrorHandler();

        headers.put("Content-Type", "application/json");

        restTemplate.setErrorHandler(errorHandler);
        latestResponse = restTemplate
                .execute("http://localhost:" + port + url, httpMethod, requestCallback, response -> {
                    if (errorHandler.hadError) {
                        return (errorHandler.getResults());
                    } else {
                        return (new ResponseResults(response));
                    }
                });
    }

    private static class ResponseResultErrorHandler implements ResponseErrorHandler {
        private ResponseResults results = null;
        private Boolean hadError = false;

        private ResponseResults getResults() {
            return results;
        }

        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            hadError = response.getRawStatusCode() >= 400;
            return hadError;
        }

        @Override
        public void handleError(@NotNull ClientHttpResponse response) throws IOException {
            results = new ResponseResults(response);
        }
    }

    protected    static String serializeUser(String confirmationPassword,int[] groupIds, String password, String username){
        StringBuilder jsonString=new StringBuilder("{");

        if (confirmationPassword != null){
            jsonString.append("\"confirmationPassword\": \"").append(confirmationPassword).append("\",");
        }
        if (groupIds!=null && groupIds.length>0){
            jsonString.append("\"groupIds\": ").append(Arrays.toString(groupIds)).append(",");
        }
        if (password != null){
            jsonString.append("\"password\": \"").append(password).append("\",");
        }
        if (username != null){
            jsonString.append("\"username\": \"").append(username).append("\",");
        }

        jsonString.append("}");

        return jsonString.toString();
    }




}
