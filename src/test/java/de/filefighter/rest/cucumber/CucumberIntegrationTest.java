package de.filefighter.rest.cucumber;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/cucumber/de/filefighter/rest/")
public class CucumberIntegrationTest {
}
