package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class CommonCucumberSteps extends CucumberIntegrationTest {

    @Given("database is empty")
    public void databaseIsEmpty() {
    }

    @And("user {long} exists")
    public void userExists(long userId) {
    }

    @And("user {long} has access token {string}")
    public void userHasAccessToken(long userId, String accessTokenValue) {
    }

    @And("user with id {long} exists and has username {string}, password {string} and refreshToken {string}")
    public void userWithIdExistsAndHasUsernamePasswordAndRefreshToken(long userId, String username, String password, String refreshTokenValue) {
    }

    // file / folder
    @Given("{string} exists with id {long} and path {string}")
    public void existsWithIdAndPath(String fileOrFolder, long fsItemId, String arg2) {
    }

    @And("user {long} is owner of file or folder with id {long}")
    public void userIsOwnerOfFileOrFolderWithId(long userId, long fsItemId) {
    }

    //key: value for json type response.
    @Then("response contains key {string} and value {string}")
    public void responseContainsKeyAndValue(String key, String value) {
    }

    @And("response contains the user with id {long}")
    public void responseContainsTheUserWithId(long userId) {
    }

    @Then("response status code is {int}")
    public void responseStatusCodeIs(int httpStatusCode) {
    }

}
