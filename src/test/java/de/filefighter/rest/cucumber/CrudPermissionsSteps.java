package de.filefighter.rest.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class CrudPermissionsSteps {
    @Given("credential are valid")
    public void credentialAreValid() {
    }

    @And("<path> exists")
    public void pathExists() {
    }

    @Given("user {int} is owner of <type> <path>")
    public void userIsOwnerOfTypePath(int arg0) {
    }

    @And("user {int} was given permisssion of <old_permission> for <type> <path>")
    public void userWasGivenPermisssionOfOld_permissionForTypePath(int arg0) {
    }

    @When("user {int} wants to change permissions of <type> <path>")
    public void userWantsToChangePermissionsOfTypePath(int arg0) {
    }

    @Then("list all users that have permissions for <type> <path>")
    public void listAllUsersThatHavePermissionsForTypePath() {
    }

    @When("user {int} changes permissions of user {int} to <new_permission>")
    public void userChangesPermissionsOfUserToNew_permission(int arg0, int arg1) {
    }

    @Then("user {int} has the following permissions for <type> <path> : <new_permission>")
    public void userHasTheFollowingPermissionsForTypePathNew_permission(int arg0) {
    }

    @And("user {int} was given permisssion of none for  <type> <path>")
    public void userWasGivenPermisssionOfNoneForTypePath(int arg0) {
    }

    @When("user {int} wants to give permissions of <new_permission> for <type> <path> to user {int}")
    public void userWantsToGivePermissionsOfNew_permissionForTypePathToUser(int arg0, int arg1) {
    }

    @And("user {int} exists")
    public void userExists(int arg0) {
    }

    @And("user {int} is not owner of <type> <path>")
    public void userIsNotOwnerOfTypePath(int arg0) {
    }
}
