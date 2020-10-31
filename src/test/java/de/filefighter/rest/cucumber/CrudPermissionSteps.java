package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CrudPermissionSteps extends SpringIntegrationTest{

    // TODO: Rearrange the steps, create Shared State Handler.

    @Given("database is empty")
    public void databaseIsEmpty() {
    }

    @And("user {int} exists")
    public void userExists(int arg0) {
    }

    @And("user {int} has access token {string}")
    public void userHasAccessToken(int arg0, String arg1) {
    }

    @And("user {int} has permission of {string} for {string} with id {int}")
    public void userHasPermissionOfForWithIdId(int arg0, String arg1, String arg2,int arg3) {
    }

    @When("user with token {string} wants to change permissions of {string} with id {int} for user with id {int} to {string}")
    public void userWithTokenWantsToChangePermissionsOfWithIdIdForUserWithIdTo(String arg0, String arg1, int fileID,int arg2, String arg3) {
    }

    @When("user with token {string} wants to remove permissions of {string} with id {int} for user {int}")
    public void userWithTokenWantsToRemovePermissionsOfWithIdIdForUser(String arg0, String arg1,int fileID, int arg2) {
    }

    @And("user with id {int} has no permission for {string} with id {int}")
    public void userWithIdHasNoPermissionForWithIdId(int arg0, String arg1, int fileID) {
    }

    @Given("{string} exists with id {int} and path {string}")
    public void existsWithIdAndPath(String arg0, int arg1, String arg2) {
    }

    @And("user {int} is owner of file or folder with id {int}")
    public void userIsOwnerOfFileOrFolderWithId(int arg0, int arg1) {
    }

    @And("user {int} has no permission for {string} with id {int}")
    public void userHasNoPermissionForWithId(int arg0, String arg1, int arg2) {
    }

    @Then("response status code is {int}")
    public void responseStatusCodeIs(int arg0) {
    }

    @Then("response message contains {string}")
    public void responseMessageContains(String arg0) {
    }

    @When("user with token {string} wants to add permissions of {string} with id {int} for user {int} for {string}")
    public void userWithTokenWantsToAddPermissionsOfWithIdForUserFor(String arg0, String arg1, int arg2, int arg3, String arg4) {
    }

}
