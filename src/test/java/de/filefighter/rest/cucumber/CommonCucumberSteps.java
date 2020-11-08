package de.filefighter.rest.cucumber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.filefighter.rest.RestApplicationIntegrationTest;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemEntity;
import de.filefighter.rest.domain.filesystem.data.persistance.FileSystemRepository;
import de.filefighter.rest.domain.token.data.persistance.AccessTokenRepository;
import de.filefighter.rest.domain.user.data.persistance.UserEntity;
import de.filefighter.rest.domain.user.data.persistance.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonCucumberSteps extends RestApplicationIntegrationTest {

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final FileSystemRepository fileSystemRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CommonCucumberSteps(UserRepository userRepository, AccessTokenRepository accessTokenRepository, FileSystemRepository fileSystemRepository) {
        this.userRepository = userRepository;
        this.accessTokenRepository = accessTokenRepository;
        this.fileSystemRepository = fileSystemRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Given("database is empty")
    public void databaseIsEmpty() {
        userRepository.deleteAll();
        accessTokenRepository.deleteAll();
        fileSystemRepository.deleteAll();
    }

    @And("user {long} exists")
    public void userExists(long userId) {
        userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .build());
    }

    @And("user with id {long} exists and has username {string}, password {string} and refreshToken {string}")
    public void userWithIdExistsAndHasUsernamePasswordAndRefreshToken(long userId, String username, String password, String refreshTokenValue) {
        userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .username(username)
                .password(password)
                .refreshToken(refreshTokenValue)
                .build());
    }

    // This step almost needs a unit test.
    @Given("{string} exists with id {long} and path {string}")
    public void fileOrFolderExistsWithIdAndPath(String fileOrFolder, long fsItemId, String path) {
        String[] names = path.split("/");
        StringBuilder completeFilePath = new StringBuilder("/");

        System.out.println(Arrays.toString(names));

        // build root dir.
        fileSystemRepository.save(FileSystemEntity
                .builder()
                .isFile(false)
                .path(completeFilePath.toString())
                .build());


        // build all files and folders.
        for (int i = 0; i < names.length; i++) {
            if (!names[i].isEmpty() && !names[i].isBlank()) {
                boolean isLastOne = i == names.length - 1;
                if(!isLastOne){
                    //is obviously a folder.
                    completeFilePath.append(names[i]).append("/");
                    fileSystemRepository.save(FileSystemEntity
                            .builder()
                            .isFile(false)
                            .path(completeFilePath.toString())
                            .build());
                    System.out.println("folder: "+completeFilePath.toString());
                }else{
                    System.out.println("last one: "+names[i]);
                    if (fileOrFolder.equals("file")) {
                        fileSystemRepository.save(FileSystemEntity
                                .builder()
                                .isFile(true)
                                .id(fsItemId)
                                .build());
                    } else if (fileOrFolder.equals("folder")) {
                        completeFilePath.append(names[i]).append("/");
                        fileSystemRepository.save(FileSystemEntity
                                .builder()
                                .isFile(false)
                                .id(fsItemId)
                                .path(completeFilePath.toString())
                                .build());
                    } else {
                        throw new IllegalArgumentException("Found not valid string for FileOrFolder in Steps file.");
                    }
                }
            }
        }
    }

    @And("user {long} is owner of file or folder with id {long}")
    public void userIsOwnerOfFileOrFolderWithId(long userId, long fsItemId) {
        FileSystemEntity fileSystemEntity = fileSystemRepository.findById(fsItemId);

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

    @And("response contains the user with id {long}")
    public void responseContainsTheUserWithId(long userId) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(latestResponse.getBody());
        long actualValue = rootNode.get("id").asLong();

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

    @And("user with id {long} exists and has username {string}, password {string}")
    public void userWithIdExistsAndHasUsernamePassword(long userId, String username, String password) {
        userRepository.save(UserEntity
                .builder()
                .userId(userId)
                .username(username)
                .password(password)
                .build());
    }
}
