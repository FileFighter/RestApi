package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ViewFolderContentsSteps extends SpringIntegrationTest{
    @When("user with token {string} wants to see the content of folder with path {string}")
    public void userWithTokenWantsToSeeTheContentOfFolderWithPath(String arg0, String arg1) {
    }

    @And("the response contains the file with id {int} and name {string}")
    public void theResponseContainsTheFileWithIdAndName(int arg0, String arg1) {
    }

    @And("in the response the file with id {int} has true for the property public")
    public void inTheResponseTheFileWithIdHasTrueForThePropertyPublic(int arg0) {
    }

    @And("the response contains an empty list for files and folders")
    public void theResponseContainsAnEmptyListForFilesAndFolders() {
    }
}
