package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class UserAuthorizationSteps {
    @And("user with id {int} exists and has username {string}, password {string} and refreshToken {string}")
    public void userWithIdExistsAndHasUsernamePasswordAndRefreshToken(int arg0, String arg1, String arg2, String arg3) {
    }

    @When("user requests login with username {string} and password {string}")
    public void userRequestsLoginWithUsernameAndPassword(String arg0, String arg1) {
    }

    @Then("response contains key {string} and value {string}")
    public void responseContainsKeyAndValue(String arg0, String arg1) {
    }

    @And("response contains the user with id {int}")
    public void responseContainsTheUserWithId(int arg0) {
    }

    @And("response status contains {string}")
    public void responseStatusContains(String arg0) {
    }

    @When("user requests accessToken with refreshToken {string} and userId {int}")
    public void userRequestsAccessTokenWithRefreshTokenAndUserId(String arg0, int arg1) {
    }

    @Then("response contains key {string} and value {int}")
    public void responseContainsKeyAndValue(String arg0, int arg1) {
    }

    @And("response contains valid accessToken")
    public void responseContainsValidAccessToken() {
    }

    @When("user requests userInfo with accessToken {string} and userId {int}")
    public void userRequestsUserInfoWithAccessTokenAndUserId(String arg0, int arg1) {
    }
}
