package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ViewFolderContentsSteps {
    @When("the user {int} requests to see the content of folder {string}")
    public void theUserRequestsToSeeTheContentOfFolderFooBar(int arg0, String path) {
    }

    @And("the folder foo\\/bar exists")
    public void theFolderFooBarExists() {
    }

    @And("the user {int} has the permission to view folder foo\\/bar")
    public void theUserHasThePermissionToViewFolderFooBar(int arg0) {
    }

    @Then("the user {int} sees a list of files and folder that are in folder foo\\/bar")
    public void theUserSeesAListOfFilesAndFolderThatAreInFolderFooBar(int arg0) {
    }

    @When("the user {int} requests to see the content of of folder foo\\/bar\\/{int}")
    public void theUserRequestsToSeeTheContentOfOfFolderFooBar(int arg0, int arg1) {
    }

    @And("the folder foo\\/bar\\/{int} does not exist")
    public void theFolderFooBarDoesNotExist(int arg0) {
    }

    @Then("the user {int} sees a notifaction that the folder foo\\/bar\\/{int} does not exist")
    public void theUserSeesANotifactionThatTheFolderFooBarDoesNotExist(int arg0, int arg1) {
    }

    @When("the user {int} requests to see the content of the folder foo\\/bar")
    public void theUserRequestsToSeeTheContentOfTheFolderFooBar(int arg0) {
    }

    @And("the user {int} does not have the permission to view the folder foo\\/bar")
    public void theUserDoesNotHaveThePermissionToViewTheFolderFooBar(int arg0) {
    }

    @Then("the user {int} sees a notifaction that the folder foo\\/bar does not exists")
    public void theUserSeesANotifactionThatTheFolderFooBarDoesNotExists(int arg0) {
    }
}
