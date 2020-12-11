package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;

public class CrudPermissionSteps extends RestApplicationIntegrationTest {

    @And("user {long} has permission of {string} for {string} with fileSystemId {long}")
    public void userHasPermissionOfForWithIdId(long userId, String readOrWrite, String fileOrFolder, long fsItemId) {
    }

    @When("user with token {string} wants to change permissions of {string} with fileSystemId {long} for user with id {long} to {string}")
    public void userWithTokenWantsToChangePermissionsOfWithIdIdForUserWithIdTo(String accessTokenValue, String fileOrFolder, long fsItemId, long userId, String newPermission) {
    }

    @When("user with token {string} wants to remove permissions of {string} with fileSystemId {long} for user {long}")
    public void userWithTokenWantsToRemovePermissionsOfWithIdIdForUser(String accessTokenValue, String fileOrFolder, long fsItemId, long userId) {
    }

    @And("user with id {long} has no permission for {string} with fileSystemId {long}")
    public void userWithIdHasNoPermissionForWithIdId(long userId, String fileOrFolder, long fsItemId) {
    }

    @And("user {long} has no permission for {string} with fileSystemId {long}")
    public void userHasNoPermissionForWithId(long userId, String fileOrFolder, long fsItemId) {
    }

    @When("user with token {string} wants to give {string} permission for {string} with fileSystemId {long} to user {long}")
    public void userWithTokenWantsToAddPermissionsOfWithIdForUserFor(String accessTokenValue, String permission, String fileOrFolder, long fsItemId, long userId) {
    }

}
