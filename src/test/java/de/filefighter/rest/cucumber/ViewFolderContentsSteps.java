package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

public class ViewFolderContentsSteps extends RestApplicationIntegrationTest {
    @When("user with token {string} wants to see the content of folder with path {string}")
    public void userWithTokenWantsToSeeTheContentOfFolderWithPath(String accessTokenValue, String path) {
    }

    @And("the response contains the file with id {long} and name {string}")
    public void theResponseContainsTheFileWithIdAndName(long fsItemId , String name) {
    }

    @And("the response contains an empty list for files and folders")
    public void theResponseContainsAnEmptyListForFilesAndFolders() {
    }
}
