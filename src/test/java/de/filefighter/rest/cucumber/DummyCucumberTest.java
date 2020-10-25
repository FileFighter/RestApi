package de.filefighter.rest.cucumber;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.Assert.assertTrue;

public class DummyCucumberTest extends SpringIntegrationTest{

    private boolean doesWork = false;

    @When("a")
    public void a() {
        doesWork = true;
    }

    @Then("b")
    public void b() {
        assertTrue(doesWork);
    }
}
