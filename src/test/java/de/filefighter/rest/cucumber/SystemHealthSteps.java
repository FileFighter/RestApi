package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.http.HttpMethod;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class SystemHealthSteps extends RestApplicationIntegrationTest {
    @When("the systemHealth endpoint is requested")
    public void theSystemHealthEndpointIsRequested() {
        executeRestApiCall(HttpMethod.GET, "health/");
    }

    @And("the user waits for {int} second\\(s)")
    public void theUserWaitsForSecondS(int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}
