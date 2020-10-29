package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ViewFolderContentsSteps {
    @And("the folder with id {string} and path {string} exists")
    public void theFolderWithIdAndPathExists(String arg0, String arg1) {
    }

    @And("the file with id {string} and path {string} exists")
    public void theFileWithIdAndPathExists(String arg0, String arg1) {
    }

    @Given("user {int} has permission to view the folder with id {int}")
    public void userHasPermissionToViewTheFolderWithId(int arg0, int arg1) {
    }

    @And("user {int} has permission to view the file with id {int}")
    public void userHasPermissionToViewTheFileWithId(int arg0, int arg1) {
    }

    @When("user with token {string} wants to see the content of folder with path {string}")
    public void userWithTokenWantsToSeeTheContentOfFolderWithPath(String arg0, String arg1) {
    }

    @Then("response status code is {int}")
    public void responseStatusCodeIs(int arg0) {
    }

    @Given("user {int} has permission to view the folder with id {string}")
    public void userHasPermissionToViewTheFolderWithId(int arg0, String arg1) {
    }

    @And("response message contains {string}")
    public void responseMessageContains(String arg0) {
    }

    @And("the response contains the file with id {int} and name {string}")
    public void theResponseContainsTheFileWithIdAndName(int arg0, String arg1) {

    }
}
