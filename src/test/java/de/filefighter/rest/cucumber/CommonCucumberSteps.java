package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistence.FileSystemRepository;
import de.filefighter.rest.domain.token.data.persistence.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistence.UserEntity;
import de.filefighter.rest.domain.user.data.persistence.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class CommonCucumberSteps extends RestApplicationIntegrationTest {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final FileSystemRepository fileSystemRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    public CommonCucumberSteps(UserRepository userRepository, AccessTokenRepository accessTokenRepository, FileSystemRepository fileSystemRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.fileSystemRepository = fileSystemRepository;
        this.objectMapper = objectMapper;
    }

    @Given("database is empty")
    public void databaseIsEmpty() {
        userRepository.deleteAll();
        accessTokenRepository.deleteAll();
        fileSystemRepository.deleteAll();
    }

    @And("user {long} exists")
    public void userExists(long userId) {
        log.info("Creating User: " + userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .build()));
    }

    @And("user with userId {long} exists and has username {string}, password {string} and refreshToken {string}")
    public void userWithIdExistsAndHasUsernamePasswordAndRefreshToken(long userId, String username, String password, String refreshTokenValue) {
        log.info("Creating User: " + userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .username(username)
                .lowercaseUsername(username.toLowerCase())
                .password(password)
                .refreshToken(refreshTokenValue)
                .build()));
    }

    @And("user with userId {long} exists and has username {string}, password {string}")
    public void userWithIdExistsAndHasUsernamePassword(long userId, String username, String password) {
        log.info("Creating User: " + userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .username(username)
                .lowercaseUsername(username.toLowerCase())
                .password(password)
                .build()));
    }

    @And("user with userId {long} is in group with groupId {long}")
    public void userWithIdIsInGroupWithId(long userId, long groupId) {
        Query query = new Query();
        Update newUpdate = new Update().set("groupIds", new long[]{groupId});
        query.addCriteria(Criteria.where("userId").is(userId));

        mongoTemplate.findAndModify(query, newUpdate, UserEntity.class);
    }

    @And("fileSystemItem with the fileSystemId {long} exists, was created by user with userId {long} has the path {string} and name {string}")
    public void fileSystemItemWithTheFileSystemIdExistsAndHasThePath(long fileSystemId, long userId, String path, String name) {
        fileSystemRepository.save(FileSystemEntity.builder()
                .path(path)
                .createdByUserId(userId)
                .fileSystemId(fileSystemId)
                .name(name)
                .build());
    }

    @And("fileSystemItem with the fileSystemId {long} exists, was created by user with userId {long} and has the name {string}")
    public void fileSystemItemWithTheFileSystemIdExistsAndHasTheName(long fileSystemId, long userId, String name) {
        fileSystemRepository.save(FileSystemEntity.builder()
                .name(name)
                .createdByUserId(userId)
                .fileSystemId(fileSystemId)
                .build());
    }

    @And("fileSystemItem with the fileSystemId {long} is a folder and contains the fileSystemId {long}")
    public void fileSystemItemWithTheFileSystemIdIsAFolderAndContainsTheFileSystemId(long fileSystemIdFolder, long fileSystemId) {
        Query query = new Query();
        Update newUpdate = new Update()
                .push("itemIds", fileSystemId)
                .set("isFile", false)
                .set("typeId", 0);
        query.addCriteria(Criteria.where("fileSystemId").is(fileSystemIdFolder));

        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    @And("fileSystemItem with the fileSystemId {long} is a folder")
    public void fileSystemItemWithTheFileSystemIdIsAFolder(long fileSystemId) {
        Query query = new Query();
        Update newUpdate = new Update().set("typeId", 0).set("isFile", false);
        query.addCriteria(Criteria.where("fileSystemId").is(fileSystemId));

        mongoTemplate.findAndModify(query, newUpdate, FileSystemEntity.class);
    }

    @And("user with userId {long} is owner of file or folder with fileSystemId {long}")
    public void userIsOwnerOfFileOrFolderWithId(long userId, long fsItemId) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findByFileSystemId(fsItemId);

        fileSystemEntity.setCreatedByUserId(userId);
        fileSystemRepository.save(fileSystemEntity);
    }

    //key: value for json type response.
    @Then("response contains key {string} and value {string}")
    public void responseContainsKeyAndValue(String key, String value) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        String actualValue = rootNode.get(key).asText();

        assertEquals(value, actualValue);
    }

    @And("response contains the user with userId {long}")
    public void responseContainsTheUserWithId(long userId) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        long actualValue = rootNode.get("userId").asLong();

        assertEquals(userId, actualValue);
    }

    @Then("response status code is {int}")
    public void responseStatusCodeIs(int httpStatusCode) throws IOException {
        assertEquals(httpStatusCode, latestResponse.getTheResponse().getRawStatusCode());
    }

    @And("response contains key {string} and value of at least {int}")
    public void responseContainsKeyAndValueOfAtLeast(String key, int value) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        int actualValue = rootNode.get(key).asInt();

        assertTrue(actualValue >= value);
    }

    @And("response contains key {string} and a different value than {string}")
    public void responseContainsKeyAndADifferentValueThan(String key, String differentValue) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        String actualValue = rootNode.get(key).asText();

        assertNotEquals(differentValue, actualValue);
    }

}
