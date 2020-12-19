package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import io.cucumber.java.en.And;

public class CrudPermissionSteps extends RestApplicationIntegrationTest {

    @And("group with the groupId {long} is allowed to VIEW the fileSystemItem with the fileSystemId {long}")
    public void groupWithTheGroupIdIsAllowedToViewTheFileSystemItemWithTheFileSystemId(long groupId, long fileSystemId) {

    }

    @And("group with the groupId {long} is allowed to EDIT the fileSystemItem with the fileSystemId {long}")
    public void groupWithTheGroupIdIsAllowedToEditTheFileSystemItemWithTheFileSystemId(long groupId, long fileSystemId) {

    }

    @And("user with the userId {long} is allowed to VIEW the fileSystemItem with the fileSystemId {long}")
    public void userWithTheUserIdIsAllowedToViewTheFileSystemItemWithTheFileSystemId(long userId, long fileSystemId) {

    }

    @And("user with the userId {long} is allowed to EDIT the fileSystemItem with the fileSystemId {long}")
    public void userWithTheUserIdIsAllowedToEditTheFileSystemItemWithTheFileSystemId(long userId, long fileSystemId) {

    }
}
