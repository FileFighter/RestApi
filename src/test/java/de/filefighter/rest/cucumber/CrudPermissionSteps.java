package de.filefighter.rest.cucumber;

import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class CrudPermissionSteps extends RestApplicationIntegrationTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @And("group with the groupId {long} is allowed to VIEW the fileSystemItem with the fileSystemId {long}")
    public void groupWithTheGroupIdIsAllowedToViewTheFileSystemItemWithTheFileSystemId(long groupId, long fileSystemId) {
        Query query = new Query();
        Update newUpdate = new Update().set("visibleForGroupIds", new long[]{groupId});
        query.addCriteria(Criteria.where("fileSystemId").is(fileSystemId));

        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    @And("group with the groupId {long} is allowed to EDIT the fileSystemItem with the fileSystemId {long}")
    public void groupWithTheGroupIdIsAllowedToEditTheFileSystemItemWithTheFileSystemId(long groupId, long fileSystemId) {
        Query query = new Query();
        Update newUpdate = new Update().set("editableForGroupIds", new long[]{groupId});
        query.addCriteria(Criteria.where("fileSystemId").is(fileSystemId));

        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    @And("user with the userId {long} is allowed to VIEW the fileSystemItem with the fileSystemId {long}")
    public void userWithTheUserIdIsAllowedToViewTheFileSystemItemWithTheFileSystemId(long userId, long fileSystemId) {
        Query query = new Query();
        Update newUpdate = new Update().set("visibleForUserIds", new long[]{userId});
        query.addCriteria(Criteria.where("fileSystemId").is(fileSystemId));

        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    @And("user with the userId {long} is allowed to EDIT the fileSystemItem with the fileSystemId {long}")
    public void userWithTheUserIdIsAllowedToEditTheFileSystemItemWithTheFileSystemId(long userId, long fileSystemId) {
        Query query = new Query();
        Update newUpdate = new Update().set("editableForUserIds", new long[]{userId});
        query.addCriteria(Criteria.where("fileSystemId").is(fileSystemId));

        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }
}
