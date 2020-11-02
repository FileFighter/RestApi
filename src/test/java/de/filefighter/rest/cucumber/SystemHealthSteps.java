package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

public class SystemHealthSteps extends RestApplicationIntegrationTest {
    @When("the systemHealth endpoint is requested")
    public void theSystemHealthEndpointIsRequested() {
        executeRestApiCall(HttpMethod.GET, "health/");
    }
}
