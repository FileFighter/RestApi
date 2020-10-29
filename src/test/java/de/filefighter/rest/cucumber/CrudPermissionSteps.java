package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CrudPermissionSteps extends SpringIntegrationTest{
    @Given("database is empty")
    public void databaseIsEmpty() {
    }

    @And("user {int} exists")
    public void userExists(int arg0) {
    }

    @And("user {int} has access token {string}")
    public void userHasAccessToken(int arg0, String arg1) {
    }

    @Given("{string} exists with id {string} and {string}")
    public void existsWithIdAnd(String arg0, String arg1, String arg2) {
    }

    @And("user {int} is owner of {string}")
    public void userIsOwnerOf(int arg0, String arg1) {
    }

    @And("user {int} has permission of {string} for {string} with {string}")
    public void userHasPermissionOfForWith(int arg0, String arg1, String arg2, String arg3) {
    }

    @When("user with token {string} wants to change permissions of {string} with id {string} for user {string} to {string}")
    public void userWithTokenWantsToChangePermissionsOfWithIdForUserTo(String arg0, String arg1, String arg2, String arg3, String arg4) {
    }

    @Then("response status code is {string}")
    public void responseStatusCodeIs(String arg0) {
    }

    @And("user with id {int} has permission {string} {string} with id {string}")
    public void userWithIdHasPermissionWithId(int arg0, String arg1, String arg2, String arg3) {
    }

    @Given("{string} exists with id {string} and path {string}")
    public void existsWithIdAndPath(String arg0, String arg1, String arg2) {
    }

    @And("user {int} has permission of {string} for {string} with id {string}")
    public void userHasPermissionOfForWithId(int arg0, String arg1, String arg2, String arg3) {
    }

    @When("user with token {string} wants to remove permissions of {string} with id {string} for user {string}")
    public void userWithTokenWantsToRemovePermissionsOfWithIdForUser(String arg0, String arg1, String arg2, String arg3) {
    }

    @And("user with id {int} has no permission for {string} with id {string}")
    public void userWithIdHasNoPermissionForWithId(int arg0, String arg1, String arg2) {
    }

    @And("user {int} has no permission for {string} with id {string}")
    public void userHasNoPermissionForWithId(int arg0, String arg1, String arg2) {
    }

    @When("user with token {string} wants to add permissions of {string} with id {string} for user {string} for {string}")
    public void userWithTokenWantsToAddPermissionsOfWithIdForUserFor(String arg0, String arg1, String arg2, String arg3, String arg4) {
    }

    @And("user with id {string} has permission {string} for {string} with id {string}")
    public void userWithIdHasPermissionForWithId(String arg0, String arg1, String arg2, String arg3) {
    }

    @And("response message cotains {string}")
    public void responseMessageCotains(String arg0) {
    }
}
